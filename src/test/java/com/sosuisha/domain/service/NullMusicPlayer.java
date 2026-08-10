package com.sosuisha.domain.service;

import java.nio.file.Path;

/**
 * A music player that does nothing. For tests that need a {@link MusicPlayer}
 * but do not care about playback.
 */
public class NullMusicPlayer implements MusicPlayer {
    @Override
    public void play(Path path) {}

    @Override
    public void stop() {}
}
