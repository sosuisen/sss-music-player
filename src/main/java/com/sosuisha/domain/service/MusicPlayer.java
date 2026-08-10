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
}
