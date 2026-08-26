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
class RepositoryExceptionTest {
    @Test
    @DisplayName("RepositoryExceptionは、非チェック例外である")
    void repository_exception_is_an_unchecked_exception() {
        var exception = new RepositoryException("Could not read the library database", null);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("RepositoryExceptionは、人間向けの文脈メッセージを持っている必要がある")
    void repository_exception_must_have_a_message_for_the_user() {
        var cause = new IOException("disk full");

        var exception = new RepositoryException("Could not read the library database", cause);

        assertEquals("Could not read the library database", exception.getMessage());
        assertThrows(NullPointerException.class, () -> new RepositoryException(null, cause));
        assertThrows(IllegalArgumentException.class, () -> new RepositoryException(" ", cause));
    }
}
