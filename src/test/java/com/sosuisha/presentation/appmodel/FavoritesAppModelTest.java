package com.sosuisha.presentation.appmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Favorite;
import com.sosuisha.domain.model.FavoriteAlbum;
import com.sosuisha.domain.model.FavoriteTrack;
import com.sosuisha.domain.repository.InMemoryFavoriteRepository;

class FavoritesAppModelTest {
    @Test
    @DisplayName("作成時に、リポジトリのお気に入りを登録順に読み込む")
    void loads_the_favorites_of_the_repository_in_order_when_created() {
        List<Favorite> saved = List.of(
            new FavoriteTrack(Path.of("a", "song.mp3")),
            new FavoriteAlbum("Album A", "Artist X")
        );

        var appModel = new FavoritesAppModel(new InMemoryFavoriteRepository(saved));

        assertEquals(saved, appModel.getFavorites());
    }

    @Test
    @DisplayName("未登録のお気に入りをトグルすると、末尾に登録され、リポジトリに保存される")
    void toggling_an_unregistered_favorite_adds_it_to_the_end_and_saves_it() {
        var repository = new InMemoryFavoriteRepository(
            List.of(new FavoriteAlbum("Album A", "Artist X"))
        );
        var appModel = new FavoritesAppModel(repository);

        appModel.toggle(new FavoriteTrack(Path.of("a", "song.mp3")));

        var expected = List.of(
            new FavoriteAlbum("Album A", "Artist X"),
            new FavoriteTrack(Path.of("a", "song.mp3"))
        );
        assertEquals(expected, appModel.getFavorites());
        assertEquals(expected, repository.findAll());
    }

    @Test
    @DisplayName("登録済みのお気に入りをトグルすると、解除され、リポジトリからも消える")
    void toggling_a_registered_favorite_removes_it_and_deletes_it_from_the_repository() {
        var repository = new InMemoryFavoriteRepository(
            List.of(
                new FavoriteAlbum("Album A", "Artist X"),
                new FavoriteTrack(Path.of("a", "song.mp3"))
            )
        );
        var appModel = new FavoritesAppModel(repository);

        appModel.toggle(new FavoriteAlbum("Album A", "Artist X"));

        var expected = List.of(new FavoriteTrack(Path.of("a", "song.mp3")));
        assertEquals(expected, appModel.getFavorites());
        assertEquals(expected, repository.findAll());
    }
}
