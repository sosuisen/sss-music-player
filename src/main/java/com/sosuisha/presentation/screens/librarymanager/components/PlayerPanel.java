package com.sosuisha.presentation.screens.librarymanager.components;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;
import com.sosuisha.domain.model.PlaybackPosition;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.presentation.screens.librarymanager.PlayerState;

import atlantafx.base.theme.Styles;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.SliderBuilder;
import io.github.sosuisen.jfxbuilder.graphics.HBoxBuilder;
import io.github.sosuisen.jfxbuilder.graphics.RegionBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

/**
 * Component that shows the playback buttons and the playing track.
 */
public class PlayerPanel {
    private PlayerPanel() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the library manager screen
     * @return panel holding the playback buttons and the playing track
     * @throws NullPointerException if viewModel is null
     */
    public static VBox getRoot(LibraryManagerViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        return VBoxBuilder
            .withChildren(
                buildButtonRow(viewModel), buildSeekRow(viewModel), buildNowPlayingRow(viewModel)
            )
            .id("playerPanel")
            .spacing(5)
            .padding(new Insets(10))
            .build();
    }

    private static HBox buildButtonRow(LibraryManagerViewModel viewModel) {
        return HBoxBuilder
            .withChildren(
                ButtonBuilder.create()
                    .graphic(new FontIcon(Material2MZ.SKIP_PREVIOUS))
                    .id("prevButton")
                    .onAction(_ -> viewModel.previousTrack())
                    .build(),
                ButtonBuilder.create()
                    .id("playButton")
                    .addStyleClass(Styles.ACCENT)
                    .graphicPropertyApply(
                        prop -> prop.bind(
                            viewModel.playerStateProperty()
                                .map(
                                    state -> state == PlayerState.PLAYING
                                        ? Material2MZ.PAUSE
                                        : Material2MZ.PLAY_ARROW
                                )
                                .map(FontIcon::new)
                        )
                    )
                    .onAction(_ -> viewModel.togglePlay())
                    .build(),
                ButtonBuilder.create()
                    .graphic(new FontIcon(Material2MZ.SKIP_NEXT))
                    .id("nextButton")
                    .onAction(_ -> viewModel.nextTrack())
                    .build(),
                ButtonBuilder.create()
                    .graphic(new FontIcon(Material2MZ.STOP))
                    .id("stopButton")
                    .onAction(_ -> viewModel.stopPlayback())
                    .build(),
                ButtonBuilder.create()
                    .id("repeatButton")
                    .graphicPropertyApply(
                        prop -> prop.bind(
                            viewModel.repeatModeProperty()
                                .map(
                                    mode -> mode == RepeatMode.ONE
                                        ? Material2MZ.REPEAT_ONE
                                        : Material2MZ.REPEAT
                                )
                                .map(FontIcon::new)
                        )
                    )
                    .onAction(_ -> viewModel.toggleRepeatMode())
                    .build(),
                RegionBuilder.create()
                    .hGrowInHBox(Priority.ALWAYS)
                    .build(),
                ButtonBuilder.create()
                    .text("Open folder")
                    .id("openFolderButton")
                    .onAction(_ -> viewModel.openTrackFolder())
                    .build()
            )
            .spacing(10)
            .alignment(Pos.CENTER_LEFT)
            .build();
    }

    private static HBox buildSeekRow(LibraryManagerViewModel viewModel) {
        var position = viewModel.playbackPositionProperty();
        var seekBar = SliderBuilder.create()
            .id("seekBar")
            .hGrowInHBox(Priority.ALWAYS)
            .disablePropertyApply(prop -> prop.bind(position.isNull()))
            .build();
        // Not bound. The user moves the value by dragging the thumb.
        position.addListener((_, _, current) -> {
            if (current == null) { return; }
            seekBar.setMax(seconds(current.length()));
            seekBar.setValue(seconds(current.position()));
        });
        // Not valueChanging. The skin changes the value without it when the
        // track is pressed. A release ends both a drag and a click.
        seekBar.setOnMouseReleased(
            _ -> viewModel.seek(Duration.ofMillis(Math.round(seekBar.getValue() * 1000)))
        );
        return HBoxBuilder
            .withChildren(
                LabelBuilder.create()
                    .id("elapsedTime")
                    .textPropertyApply(
                        prop -> prop.bind(timeOf(position, PlaybackPosition::position))
                    )
                    .build(),
                seekBar,
                LabelBuilder.create()
                    .id("trackLength")
                    .textPropertyApply(
                        prop -> prop.bind(timeOf(position, PlaybackPosition::length))
                    )
                    .build()
            )
            .spacing(10)
            .alignment(Pos.CENTER_LEFT)
            .build();
    }

    private static double seconds(Duration time) {
        return time.toMillis() / 1000.0;
    }

    // The time is 0:00 while the position is not known.
    private static ObservableValue<String> timeOf(
        ObservableValue<@Nullable PlaybackPosition> position,
        Function<PlaybackPosition, Duration> part) {
        return Bindings.createStringBinding(() -> {
            var current = position.getValue();
            return PlaybackTime.format(current == null ? Duration.ZERO : part.apply(current));
        }, position);
    }

    private static HBox buildNowPlayingRow(LibraryManagerViewModel viewModel) {
        return HBoxBuilder
            .withChildren(
                LabelBuilder.create()
                    .id("playerTitle")
                    .textPropertyApply(
                        prop -> prop.bind(
                            viewModel.selectedTrackProperty().map(track -> track.tag().title())
                        )
                    )
                    .build(),
                LabelBuilder.create()
                    .id("playerArtist")
                    .textPropertyApply(
                        prop -> prop.bind(
                            viewModel.selectedTrackProperty().map(track -> track.tag().artist())
                        )
                    )
                    .build()
            )
            .spacing(10)
            .alignment(Pos.CENTER_LEFT)
            .build();
    }
}
