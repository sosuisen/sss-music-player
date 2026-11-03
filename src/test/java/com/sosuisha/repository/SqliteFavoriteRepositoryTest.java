package com.sosuisha.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;

class SqliteFavoriteRepositoryTest {
    @TempDir
    Path folder;

    @Test
    @DisplayName("空のDBでは、お気に入りの一覧は空である")
    void the_favorite_list_of_an_empty_database_is_empty() {
        var repository = new SqliteFavoriteRepository(folder.resolve("library.db"));

        assertEquals(List.of(), repository.findAll());
    }

    @Test
    @DisplayName("追加したアルバムと曲のお気に入りを、DBを開き直しても追加した順に返す")
    void find_all_returns_added_albums_and_tracks_in_the_order_they_were_added_after_reopening() {
        var file = folder.resolve("library.db");
        var repository = new SqliteFavoriteRepository(file);
        repository.add(new FavoriteTrack(Path.of("b", "song.mp3")));
        repository.add(new FavoriteAlbum("Album A", "Artist X"));
        repository.add(new FavoriteTrack(Path.of("a", "song.mp3")));

        var reopened = new SqliteFavoriteRepository(file);

        assertEquals(
            List.of(
                new FavoriteTrack(Path.of("b", "song.mp3")),
                new FavoriteAlbum("Album A", "Artist X"),
                new FavoriteTrack(Path.of("a", "song.mp3"))
            ),
            reopened.findAll()
        );
    }

    @Test
    @DisplayName("削除したお気に入りは一覧から消え、他のお気に入りは残る")
    void a_removed_favorite_disappears_from_the_list_and_the_others_remain() {
        var repository = new SqliteFavoriteRepository(folder.resolve("library.db"));
        repository.add(new FavoriteAlbum("Album A", "Artist X"));
        repository.add(new FavoriteTrack(Path.of("a", "song.mp3")));
        repository.add(new FavoriteAlbum("Album B", "Artist Y"));

        repository.remove(new FavoriteAlbum("Album A", "Artist X"));
        repository.remove(new FavoriteTrack(Path.of("a", "song.mp3")));

        assertEquals(List.of(new FavoriteAlbum("Album B", "Artist Y")), repository.findAll());
    }
}
