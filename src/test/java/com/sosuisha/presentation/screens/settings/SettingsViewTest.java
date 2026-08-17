package com.sosuisha.presentation.screens.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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
import com.sosuisha.domain.model.Theme;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.repository.SettingsRepositoryImpl;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class SettingsViewTest {
    @TempDir
    Path folder;

    private SettingsAppModel appModel;
    private Stage stage;

    @Start
    void setup(Stage stage) {
        this.stage = stage;
        System.setProperty(
            "sss.settings.file",
            folder.resolve("settings.properties").toString()
        );
        appModel = new SettingsAppModel(new SettingsRepositoryImpl());
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
    @DisplayName("設定ウィンドウのテーマのプルダウンに、選択肢として全テーマが表示される")
    void the_theme_pulldown_in_the_settings_window_shows_all_themes_as_choices(FxRobot robot) {
        var themePulldown = robot.lookup("#theme").queryAs(ComboBox.class);

        assertEquals(
            List.of(
                "Primer Light", "Primer Dark", "Nord Light", "Nord Dark",
                "Cupertino Light", "Cupertino Dark", "Dracula"
            ),
            themePulldown.getItems().stream().map(Object::toString).toList()
        );
    }

    @Test
    @DisplayName("テーマのプルダウンの初期値は、現在のテーマである")
    void the_initial_value_of_the_theme_pulldown_is_the_current_theme(FxRobot robot) {
        robot.interact(() -> {
            appModel.themeProperty().set(Theme.NORD_DARK);
            var view = new SettingsView(
                new SettingsViewModel(appModel, _ -> Optional.empty())
            );
            stage.setScene(view.getScene());
        });

        var themePulldown = robot.lookup("#theme").queryAs(ComboBox.class);
        assertEquals(Theme.NORD_DARK, themePulldown.getValue());
    }

    @Test
    @DisplayName("設定を変更すると、表示中の音楽ライブラリのパスも更新される")
    void the_shown_music_library_path_is_updated_when_the_settings_are_changed(FxRobot robot) {
        robot.interact(() -> appModel.setSettings(new Settings(Path.of("changed"))));

        verifyThat("#musicLibraryPath", LabeledMatchers.hasText("changed"));
    }

    @Test
    @DisplayName("エラーが発生すると、設定ウィンドウにエラーメッセージが表示される")
    void the_error_message_is_shown_in_the_settings_window_when_an_error_occurs(FxRobot robot) {
        robot.interact(() -> appModel.errorProperty().set(new IOException("read-only-path")));

        verifyThat(
            "#errorMessage",
            LabeledMatchers.hasText("Failed to save the settings file: read-only-path")
        );
    }

    @Test
    @DisplayName("ウィンドウの幅を広げると、広がった分は音楽ライブラリのパスの列が受け取る")
    void the_music_library_path_column_takes_all_the_extra_width_when_the_window_gets_wider(
        FxRobot robot) {
        var button = robot.lookup("#selectFolder").queryAs(Button.class);
        var buttonXBefore = button.getLayoutX();

        robot.interact(() -> stage.setWidth(stage.getWidth() + 200));

        assertEquals(buttonXBefore + 200, button.getLayoutX(), 0.001);
    }

    @Test
    @DisplayName("フォルダを選択するボタンを押すと、選択したフォルダのパスが設定ファイルに保存される")
    void clicking_the_select_folder_button_saves_the_selected_folder_path_to_the_settings_file(
        FxRobot robot) throws Exception {
        robot.clickOn("#selectFolder");

        assertEquals(new Settings(Path.of("selected")), new SettingsRepositoryImpl().load());
    }
}
