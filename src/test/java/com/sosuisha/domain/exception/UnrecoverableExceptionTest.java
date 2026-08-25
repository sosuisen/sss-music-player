package com.sosuisha.domain.exception;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnrecoverableExceptionTest {
    @Test
    @DisplayName("RepositoryException・LibraryScanException・SettingsExceptionは、UnrecoverableExceptionのサブクラスである")
    void the_repository_library_scan_and_settings_exceptions_are_unrecoverable_exceptions() {
        assertInstanceOf(UnrecoverableException.class, new RepositoryException("message", null));
        assertInstanceOf(UnrecoverableException.class, new LibraryScanException("message", null));
        assertInstanceOf(UnrecoverableException.class, new SettingsException("message", null));
    }

    @Test
    @DisplayName("UnrecoverableExceptionは、非チェック例外であり、人間向けの文脈メッセージを持っている必要がある")
    void unrecoverable_exception_is_unchecked_and_must_have_a_message_for_the_user() {
        var exception = new UnrecoverableException("message", null) {};

        assertInstanceOf(RuntimeException.class, exception);
        assertThrows(NullPointerException.class, () -> new UnrecoverableException(null, null) {});
        assertThrows(
            IllegalArgumentException.class, () -> new UnrecoverableException(" ", null) {}
        );
    }
}
