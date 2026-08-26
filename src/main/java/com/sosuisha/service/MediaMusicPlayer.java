package com.sosuisha.service;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.sosuisha.domain.service.MusicPlayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Plays audio files with the JavaFX media framework. Starting a new file stops
 * the file that is currently playing.
 */
public class MediaMusicPlayer implements MusicPlayer {
    /** The player of the loaded audio file and the path of that file. */
    private record Playing(MediaPlayer player, Path path) {
    }

    // Null while nothing is loaded.
    private @Nullable Playing playing;
    // The initial callback does nothing.
    private Runnable onFinished = () -> {
    };

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
        var player = new MediaPlayer(new Media(path.toUri().toString()));
        player.setOnEndOfMedia(onFinished);
        player.play();
        playing = new Playing(player, path);
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
}
