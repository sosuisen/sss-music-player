package com.sosuisha.presentation.screens.librarymanager.components;

import java.time.Duration;
import java.util.Objects;

/**
 * Formats a playback time for the player panel.
 */
public final class PlaybackTime {
    private PlaybackTime() {}

    /**
     * Formats the time as minutes and seconds, such as {@code 1:23}. A time of
     * an hour or more also shows the hours, such as {@code 1:02:03}.
     *
     * @param time time to format
     * @return the formatted time
     * @throws NullPointerException if time is null
     */
    public static String format(Duration time) {
        Objects.requireNonNull(time, "time must not be null");
        if (time.toHours() > 0) {
            return "%d:%02d:%02d".formatted(
                time.toHours(), time.toMinutesPart(), time.toSecondsPart()
            );
        }
        return "%d:%02d".formatted(time.toMinutes(), time.toSecondsPart());
    }
}
