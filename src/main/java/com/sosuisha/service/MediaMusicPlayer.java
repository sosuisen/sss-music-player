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
import java.util.function.Consumer;

import com.sosuisha.domain.service.MusicPlayer;

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

    /** The player of the loaded audio file and the path of that file. */
    private record Playing(MediaPlayer player, Path path) {
    }

    /** An m4a file and the temporary WAV file converted from it. */
    private record Converted(Path source, Path wav) {
    }

    // Null while nothing is loaded.
    private @Nullable Playing playing;
    // The last m4a converted and its temporary WAV. Null before the first m4a.
    private @Nullable Converted converted;
    // The initial callback does nothing.
    private Runnable onFinished = () -> {
    };
    // The initial callback does nothing.
    private Consumer<String> onError = _ -> {
    };
    private final WavConverter wavConverter;
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
        this.wavConverter = Objects.requireNonNull(wavConverter, "wavConverter must not be null");
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
        var file = path;
        if (path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".m4a")) {
            try {
                if (converted == null) {
                    converted = new Converted(path, wavConverter.toWav(path));
                }
                file = converted.wav();
            } catch (IOException e) {
                onError.accept(e.getMessage());
                return;
            }
        }
        MediaPlayer player;
        try {
            player = new MediaPlayer(new Media(mediaUri(file)));
        } catch (MediaException e) {
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
        player.play();
        playing = new Playing(player, path);
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
        if (playing == null) { return; }
        playing.player().stop();
        playing.player().dispose();
        playing = null;
    }

    /**
     * Pauses the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    @Override
    public void pause() {
        if (playing == null) { return; }
        playing.player().pause();
    }

    /**
     * Resumes the paused audio file from the paused position. Does nothing
     * when nothing is paused.
     */
    @Override
    public void resume() {
        if (playing == null) { return; }
        playing.player().play();
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
        return Optional.ofNullable(playing).map(Playing::path);
    }

    // For the tests. It shows which file JavaFX plays.
    Optional<MediaPlayer> mediaPlayer() {
        return Optional.ofNullable(playing).map(Playing::player);
    }
}
