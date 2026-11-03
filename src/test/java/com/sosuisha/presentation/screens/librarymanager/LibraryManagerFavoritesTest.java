package com.sosuisha.presentation.screens.librarymanager;

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

import javafx.scene.Node;
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
}
