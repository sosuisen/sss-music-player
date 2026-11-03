package com.sosuisha.main;

import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import com.sosuisha.domain.model.FavoriteTrack;
import com.sosuisha.presentation.screens.librarymanager.SortKey;
import com.sosuisha.repository.SqliteFavoriteRepository;

import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class AppWithFavoritesTest {
    @Start
    void setup(Stage stage) throws Exception {
        var folder = Files.createTempDirectory("sss-music-player-test");
        var musicFolder = folder.resolve("music");
        Files.createDirectories(musicFolder);
        var song = Files.createFile(musicFolder.resolve("song1.mp3"));
        Files.createFile(musicFolder.resolve("song2.mp3"));
        var database = folder.resolve("library.db");
        new SqliteFavoriteRepository(database).add(new FavoriteTrack(song));
        var file = folder.resolve("settings.properties");
        var properties = new Properties();
        properties.setProperty("musicLibraryPath", musicFolder.toString());
        try (var writer = Files.newBufferedWriter(file)) {
            properties.store(writer, null);
        }
        System.setProperty("sss.settings.file", file.toString());
        System.setProperty("sss.library.db", database.toString());
        // The injected primary stage is reused across tests and rejects
        // initStyle, so App gets a fresh stage.
        new App().start(new Stage());
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
        System.clearProperty("sss.library.db");
    }

    @Test
    @DisplayName("起動時に、ライブラリDBのお気に入りを読み込み、Favorites表示に並べる")
    void the_favorites_in_the_library_database_are_listed_in_favorites_at_startup(
        FxRobot robot) throws Exception {
        ComboBox<SortKey> sortKey = robot.lookup("#sortKey").queryComboBox();
        robot.interact(() -> sortKey.setValue(SortKey.FAVORITES));

        // The scan runs in the background, so wait until the result arrives.
        WaitForAsyncUtils.waitFor(
            5,
            TimeUnit.SECONDS,
            () -> robot.lookup("#albumList").queryListView().getItems().size() == 1
        );
        verifyThat("#albumList", ListViewMatchers.hasItems(1));
    }
}
