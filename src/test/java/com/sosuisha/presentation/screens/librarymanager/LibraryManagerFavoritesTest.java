package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.AlbumItem;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.TrackItem;
import com.sosuisha.presentation.screens.librarymanager.components.FavoriteStar;

import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.stage.Stage;

class LibraryManagerFavoritesTest extends LibraryManagerViewTestBase {
    private static final MusicFile ONE = new MusicFile(
        Path.of("a", "one.mp3"), 100, new TrackMetadata("One", "X", "Album A", "X", "1", "")
    );

    @Start
    void setup(Stage stage) {
        setUpLibraryManager(stage);
    }

    @Test
    @DisplayName("アルバム行の☆をクリックすると、そのアルバムがお気に入りに登録される")
    void clicking_the_star_of_an_album_row_registers_the_album_as_a_favorite(FxRobot robot) {
        robot.interact(() -> viewModel.setFiles(List.of(ONE)));

        Node star = robot.lookup("#albumList .favorite-star").query();
        robot.clickOn(star);

        assertTrue(viewModel.isFavorite(new AlbumItem(new Album("Album A", "X", List.of(ONE)))));
    }

    @Test
    @DisplayName("アルバムを登録すると、アルバム行の星は登録済みの表示になる")
    void registering_an_album_shows_the_star_of_its_row_as_a_favorite(FxRobot robot) {
        robot.interact(() -> viewModel.setFiles(List.of(ONE)));

        robot.clickOn(robot.lookup("#albumList .favorite-star").<Node>query());

        Labeled star = robot.lookup("#albumList .favorite-star").query();
        assertEquals(FavoriteStar.glyph(true), star.getText());
    }

    @Test
    @DisplayName("曲行の☆をクリックすると、その曲がお気に入りに登録される")
    void clicking_the_star_of_a_track_row_registers_the_track_as_a_favorite(FxRobot robot) {
        robot.interact(() -> {
            viewModel.setFiles(List.of(ONE));
            viewModel.selectAlbum(new Album("Album A", "X", List.of(ONE)));
        });

        Node star = robot.lookup("#trackList .favorite-star").query();
        robot.clickOn(star);

        assertTrue(viewModel.isFavorite(new TrackItem(ONE)));
    }
}
