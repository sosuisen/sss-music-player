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
}
