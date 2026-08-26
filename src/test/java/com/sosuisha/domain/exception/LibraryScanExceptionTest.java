package com.sosuisha.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NullUnmarked;

// These tests pass null on purpose to check the runtime contract.
@NullUnmarked
class LibraryScanExceptionTest {
    @Test
    @DisplayName("LibraryScanExceptionは、非チェック例外である")
    void library_scan_exception_is_an_unchecked_exception() {
        var exception = new LibraryScanException("Could not scan the music library folder", null);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("LibraryScanExceptionは、人間向けの文脈メッセージを持っている必要がある")
    void library_scan_exception_must_have_a_message_for_the_user() {
        var cause = new IOException("disk error");

        var exception = new LibraryScanException("Could not scan the music library folder", cause);

        assertEquals("Could not scan the music library folder", exception.getMessage());
        assertThrows(NullPointerException.class, () -> new LibraryScanException(null, cause));
        assertThrows(IllegalArgumentException.class, () -> new LibraryScanException(" ", cause));
    }
}
