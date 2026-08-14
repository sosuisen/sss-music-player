package com.sosuisha.domain.service;

import java.nio.file.Path;

/**
 * Plays audio files.
 */
public interface MusicPlayer {
    /**
     * Plays the audio file at the given path.
     *
     * @param path path of the audio file to play
     */
    void play(Path path);

    /**
     * Stops the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    void stop();

    /**
     * Pauses the audio file that is currently playing. Does nothing when
     * nothing is playing.
     */
    void pause();

    /**
     * Resumes the paused audio file from the paused position. Does nothing
     * when nothing is paused.
     */
    void resume();

    /**
     * Sets the callback that is invoked when the playing audio file reaches
     * its end.
     *
     * @param onFinished callback invoked at the end of the audio file
     */
    void setOnFinished(Runnable onFinished);
}
