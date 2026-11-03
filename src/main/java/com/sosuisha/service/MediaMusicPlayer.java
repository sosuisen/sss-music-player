package com.sosuisha.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.sosuisha.domain.service.MusicPlayer;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Plays audio files with the JavaFX media framework. Starting a new file stops
 * the file that is currently playing.
 */
public class MediaMusicPlayer implements MusicPlayer {
    private static final HexFormat HEX = HexFormat.of().withUpperCase();

    /**
     * The loaded audio file. Its player is null while an m4a file is being
     * converted.
     */
    private static final class Loaded {
        private final Path path;
        private @Nullable MediaPlayer player;
        // Set by pause() and cleared by resume(), also during the conversion.
        private boolean paused;

        private Loaded(Path path) {
            this.path = path;
        }
    }

    /** An m4a file and the temporary WAV file converted from it. */
    private record Converted(Path source, Path wav) {
    }

    // Null while nothing is loaded.
    private @Nullable Loaded loaded;
    // The last m4a converted and its temporary WAV. Null before the first m4a.
    private @Nullable Converted converted;
    // The initial callback does nothing.
    private Runnable onFinished = () -> {
    };
    // The initial callback does nothing.
    private Consumer<String> onError = _ -> {
    };
    private final WavConverter wavConverter;
    // Runs the conversions off the JavaFX application thread. A conversion
    // takes about 1.4 seconds for a 4-minute track. See ADR 005.
    private final Executor converterExecutor;
    // Temporary WAVs that are no longer played but not deleted yet. They are
    // tried again at each play. See ADR 006.
    private final List<Path> unusedWavs = new ArrayList<>();

    /**
     * Creates a player that converts m4a files into the default folder of
     * {@link WavConverter}.
     */
    public MediaMusicPlayer() {
        this(new WavConverter());
    }

    /**
     * Creates a player that converts m4a files with the given converter.
     *
     * @param wavConverter converter of m4a files into temporary WAV files
     * @throws NullPointerException if wavConverter is null
     */
    public MediaMusicPlayer(WavConverter wavConverter) {
        this(wavConverter, task -> Thread.ofVirtual().start(task));
    }

    // The tests pass an executor that runs the conversion when they choose.
    MediaMusicPlayer(WavConverter wavConverter, Executor converterExecutor) {
        this.wavConverter = Objects.requireNonNull(wavConverter, "wavConverter must not be null");
        this.converterExecutor = Objects.requireNonNull(
            converterExecutor, "converterExecutor must not be null"
        );
    }

    /**
     * Plays the audio file at the given path.
     *
     * @param path path of the audio file to play
     * @throws NullPointerException if path is null
     */
    @Override
    public void play(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        stop();
        if (converted != null && !converted.source().equals(path)) {
            unusedWavs.add(converted.wav());
            converted = null;
        }
        unusedWavs.removeIf(WavConverter::tryDelete);
        var request = new Loaded(path);
        loaded = request;
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".m4a")) {
            open(request, path);
        } else if (converted != null) {
            open(request, converted.wav());
        } else {
            converterExecutor.execute(() -> convert(request));
        }
    }

    // Runs off the JavaFX application thread.
    private void convert(Loaded request) {
        try {
            var wav = wavConverter.toWav(request.path);
            Platform.runLater(() -> finishConversion(request, wav));
        } catch (IOException e) {
            var reason = e.getMessage();
            Platform.runLater(() -> failConversion(request, reason));
        }
    }

    private void finishConversion(Loaded request, Path wav) {
        if (loaded != request) {
            // The player stopped or started another file during the conversion.
            unusedWavs.add(wav);
            unusedWavs.removeIf(WavConverter::tryDelete);
            return;
        }
        converted = new Converted(request.path, wav);
        open(request, wav);
    }

    private void failConversion(Loaded request, @Nullable String reason) {
        if (loaded != request) { return; }
        loaded = null;
        onError.accept(reason);
    }

    private void open(Loaded request, Path file) {
        MediaPlayer player;
        try {
            player = new MediaPlayer(new Media(mediaUri(file)));
        } catch (MediaException e) {
            loaded = null;
            onError.accept(e.getMessage());
            return;
        }
        player.setOnEndOfMedia(onFinished);
        player.setOnError(() -> {
            // getError() is set whenever onError runs.
            var reason = player.getError().getMessage();
            stop();
            onError.accept(reason);
        });
        // A pause during the conversion leaves the player ready but not
        // started. resume() starts it.
        if (!request.paused) {
            player.play();
        }
        request.player = player;
    }

    // Media converts the URI with URI.toASCIIString(), which normalizes the
    // non-ASCII characters to NFC. A file whose path is in NFD is then not
    // found, so the characters are percent-encoded here as they are.
    static String mediaUri(Path path) {
        var ascii = new StringBuilder();
        for (byte b : path.toUri().toString().getBytes(StandardCharsets.UTF_8)) {
            if (b >= 0) {
                ascii.append((char) b);
            } else {
                ascii.append('%').append(HEX.toHexDigits(b));
            }
        }
        return ascii.toString();
    }

    /**
     * Stops the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    @Override
    public void stop() {
        if (loaded == null) { return; }
        var player = loaded.player;
        if (player != null) {
            player.stop();
            player.dispose();
        }
        loaded = null;
    }

    /**
     * Pauses the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    @Override
    public void pause() {
        if (loaded == null) { return; }
        loaded.paused = true;
        if (loaded.player != null) {
            loaded.player.pause();
        }
    }

    /**
     * Resumes the paused audio file from the paused position. Does nothing
     * when nothing is paused.
     */
    @Override
    public void resume() {
        if (loaded == null) { return; }
        loaded.paused = false;
        if (loaded.player != null) {
            loaded.player.play();
        }
    }

    /**
     * Sets the callback that is invoked when the playing audio file reaches
     * its end. It applies to files played after this call.
     *
     * @param onFinished callback invoked at the end of the audio file
     * @throws NullPointerException if onFinished is null
     */
    @Override
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = Objects.requireNonNull(onFinished, "onFinished must not be null");
    }

    /**
     * Sets the callback that is invoked when an audio file cannot be played.
     * It applies to files played after this call.
     *
     * @param onError callback invoked with the reason of the failure
     * @throws NullPointerException if onError is null
     */
    @Override
    public void setOnError(Consumer<String> onError) {
        this.onError = Objects.requireNonNull(onError, "onError must not be null");
    }

    /**
     * Returns the path of the audio file loaded in the player, whether it is
     * playing or paused.
     *
     * @return path of the loaded audio file, or an empty Optional when nothing
     *     is loaded
     */
    @Override
    public Optional<Path> playingPath() {
        return Optional.ofNullable(loaded).map(current -> current.path);
    }

    // For the tests. It shows which file JavaFX plays.
    Optional<MediaPlayer> mediaPlayer() {
        var current = loaded;
        return current == null ? Optional.empty() : Optional.ofNullable(current.player);
    }
}
