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
}
