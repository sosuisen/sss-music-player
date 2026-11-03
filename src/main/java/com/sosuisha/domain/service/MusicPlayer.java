package com.sosuisha.domain.service;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import com.sosuisha.domain.model.PlaybackPosition;

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
     * Moves the playback of the loaded audio file to the given position. The
     * playback stays paused when it is paused. Does nothing when nothing is
     * loaded.
     *
     * @param position time from the start of the file
     * @throws NullPointerException if position is null
     */
    void seek(Duration position);

    /**
     * Sets the callback that is invoked when the playing audio file reaches
     * its end.
     *
     * @param onFinished callback invoked at the end of the audio file
     * @throws NullPointerException if onFinished is null
     */
    void setOnFinished(Runnable onFinished);

    /**
     * Sets the callback that is invoked when an audio file cannot be played.
     * The callback receives the reason of the failure.
     *
     * @param onError callback invoked with the reason of the failure
     * @throws NullPointerException if onError is null
     */
    void setOnError(Consumer<String> onError);

    /**
     * Sets the callback that is invoked while the loaded audio file plays.
     * The callback receives the position of the playback and the length of
     * the file.
     *
     * @param onProgress callback invoked with the playback position
     * @throws NullPointerException if onProgress is null
     */
    void setOnProgress(Consumer<PlaybackPosition> onProgress);

    /**
     * Returns the path of the audio file loaded in the player, whether it is
     * playing or paused.
     *
     * @return path of the loaded audio file, or an empty Optional when nothing
     *     is loaded
     */
    Optional<Path> playingPath();
}
