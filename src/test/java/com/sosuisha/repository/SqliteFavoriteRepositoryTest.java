package com.sosuisha.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteFavoriteRepositoryTest {
    @TempDir
    Path folder;

    @Test
    @DisplayName("空のDBでは、お気に入りの一覧は空である")
    void the_favorite_list_of_an_empty_database_is_empty() {
        var repository = new SqliteFavoriteRepository(folder.resolve("library.db"));

        assertEquals(List.of(), repository.findAll());
    }
}
