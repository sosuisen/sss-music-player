package com.sosuisha.presentation.screens.librarymanager.components;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlaybackTimeTest {
    @Test
    @DisplayName("再生時間を「分:秒」で表す（0:00、1:23、59:59）")
    void a_playback_time_is_shown_as_minutes_and_seconds() {
        assertAll(
            () -> assertEquals("0:00", PlaybackTime.format(Duration.ZERO)),
            () -> assertEquals("1:23", PlaybackTime.format(Duration.ofSeconds(83))),
            () -> assertEquals("59:59", PlaybackTime.format(Duration.ofSeconds(3599)))
        );
    }

    @Test
    @DisplayName("1時間以上の再生時間は「時:分:秒」で表す（1:02:03）")
    void a_playback_time_of_an_hour_or_more_is_shown_as_hours_minutes_and_seconds() {
        assertAll(
            () -> assertEquals("1:00:00", PlaybackTime.format(Duration.ofHours(1))),
            () -> assertEquals("1:02:03", PlaybackTime.format(Duration.ofSeconds(3723)))
        );
    }
}
