package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import com.sosuisha.domain.model.Settings;
import com.sosuisha.presentation.appmodel.SettingsAppModel;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class SettingsViewTest {
    private SettingsAppModel appModel;
    private SettingsView view;

    @Start
    void setup(Stage stage) {
        appModel = new SettingsAppModel();
        appModel.setSettings(new Settings(Path.of("music")));
        view = new SettingsView(new SettingsViewModel(appModel));
        stage.setScene(view.getScene());
        stage.setTitle(view.getTitle());
        stage.show();
    }

    @Test
    @DisplayName("設定ウィンドウに音楽ライブラリのパスが表示される")
    void window_shows_the_music_library_path() {
        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("music"));
    }

    @Test
    @DisplayName("設定ウィンドウにライブラリのパスの見出しラベルが表示される")
    void window_shows_the_library_path_caption() {
        verifyThat("Library path:", NodeMatchers.isVisible());
    }

    @Test
    @DisplayName("設定ウィンドウにフォルダを選択するボタンが表示される")
    void window_shows_the_select_folder_button() {
        verifyThat("#selectFolder", LabeledMatchers.hasText("Select folder..."));
    }

    @Test
    @DisplayName("設定ウィンドウのシーンは400x300の大きさである")
    void the_scene_of_the_settings_window_is_400_by_300() {
        assertEquals(400, view.getScene().getWidth());
        assertEquals(300, view.getScene().getHeight());
    }

    @Test
    @DisplayName("設定を変更すると、表示中の音楽ライブラリのパスも更新される")
    void the_shown_music_library_path_is_updated_when_the_settings_are_changed(FxRobot robot) {
        robot.interact(() -> appModel.setSettings(new Settings(Path.of("changed"))));

        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("changed"));
    }
}
