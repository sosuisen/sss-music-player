package com.sosuisha.service;

import java.nio.file.Path;
import java.util.Objects;

import com.sosuisha.domain.service.MusicPlayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Plays audio files with the JavaFX media framework. Starting a new file stops
 * the file that is currently playing.
 */
public class MediaMusicPlayer implements MusicPlayer {
    private MediaPlayer mediaPlayer;
    private Runnable onFinished;

    /**
     * Plays the audio file at the given path.
     *
     * @param path path of the audio file to play
     * @throws NullPointerException if path is null
     */
    @Override
    public void play(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        mediaPlayer = new MediaPlayer(new Media(path.toUri().toString()));
        if (onFinished != null) {
            mediaPlayer.setOnEndOfMedia(onFinished);
        }
        mediaPlayer.play();
    }

    /**
     * Stops the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    @Override
    public void stop() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = null;
    }

    /**
     * Pauses the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    @Override
    public void pause() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.pause();
    }

    /**
     * Resumes the paused audio file from the paused position. Does nothing
     * when nothing is paused.
     */
    @Override
    public void resume() {
        if (mediaPlayer == null) { return; }
        mediaPlayer.play();
    }

    /**
     * Sets the callback that is invoked when the playing audio file reaches
     * its end. It applies to files played after this call.
     *
     * @param onFinished callback invoked at the end of the audio file
     */
    @Override
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }
}
