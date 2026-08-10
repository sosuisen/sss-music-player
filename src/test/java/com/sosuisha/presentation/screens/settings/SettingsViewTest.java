package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.SettingsRepository;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class SettingsViewTest {
    @TempDir
    Path folder;

    private SettingsAppModel appModel;

    @Start
    void setup(Stage stage) {
        System.setProperty(
            "sss.settings.file",
            folder.resolve("settings.properties").toString()
        );
        appModel = new SettingsAppModel(new SettingsRepository());
        appModel.setSettings(new Settings(Path.of("music")));
        var view = new SettingsView(
            new SettingsViewModel(appModel, _ -> Optional.of(Path.of("selected")))
        );
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("sss.settings.file");
    }

    @Test
    @DisplayName("設定ウィンドウに音楽ライブラリのパスが表示される")
    void window_shows_the_music_library_path() {
        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("music"));
    }

    @Test
    @DisplayName("設定を変更すると、表示中の音楽ライブラリのパスも更新される")
    void the_shown_music_library_path_is_updated_when_the_settings_are_changed(FxRobot robot) {
        robot.interact(() -> appModel.setSettings(new Settings(Path.of("changed"))));

        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("changed"));
    }

    @Test
    @DisplayName("フォルダを選択するボタンを押すと、選択したフォルダのパスが設定ファイルに保存される")
    void clicking_the_select_folder_button_saves_the_selected_folder_path_to_the_settings_file(
        FxRobot robot) throws Exception {
        robot.clickOn("#selectFolder");

        assertEquals(new Settings(Path.of("selected")), new SettingsRepository().load());
    }
}
