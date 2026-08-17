package com.sosuisha.presentation.screens.librarymanager.components;

import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.graphics.HBoxBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Component that shows the album info panel and the track list of the
 * selected album.
 */
public class TrackPane {
    private TrackPane() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the library manager screen
     * @return pane holding the album info panel and the track list
     * @throws NullPointerException if viewModel is null
     */
    public static VBox getRoot(LibraryManagerViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        var trackList = buildTrackList(viewModel);
        VBox.setVgrow(trackList, Priority.ALWAYS);
        return VBoxBuilder
            .withChildren(buildAlbumInfoPanel(viewModel), trackList)
            .build();
    }

    private static HBox buildAlbumInfoPanel(LibraryManagerViewModel viewModel) {
        return HBoxBuilder
            .withChildren(
                LabelBuilder.create()
                    .id("albumInfoName")
                    .textPropertyApply(
                        prop -> prop.bind(viewModel.selectedAlbumProperty().map(Album::name))
                    )
                    .build(),
                LabelBuilder.create()
                    .id("albumInfoArtist")
                    .textPropertyApply(
                        prop -> prop.bind(viewModel.selectedAlbumProperty().map(Album::artist))
                    )
                    .build(),
                LabelBuilder.create()
                    .id("albumInfoYear")
                    .textPropertyApply(
                        prop -> prop.bind(viewModel.selectedAlbumProperty().map(Album::year))
                    )
                    .build(),
                ButtonBuilder.create()
                    .text("Edit")
                    .id("editAlbumButton")
                    .disablePropertyApply(
                        prop -> prop.bind(viewModel.selectedAlbumProperty().isNull())
                    )
                    .onAction(_ -> viewModel.openAlbumEditWindow())
                    .build()
            )
            .spacing(10)
            .padding(new Insets(5))
            .alignment(Pos.CENTER_LEFT)
            .build();
    }

    private static ListView<MusicFile> buildTrackList(LibraryManagerViewModel viewModel) {
        return ListViewBuilder.create(viewModel.getSelectedTracks())
            .id("trackList")
            .cellFactory(_ -> new ListCell<>() {
                {
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && getItem() != null) {
                            viewModel.playTrack(getItem());
                        }
                    });
                }

                @Override
                protected void updateItem(MusicFile item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : viewModel.trackRowText(item));
                }
            })
            .apply(listView -> {
                listView.getSelectionModel().selectedItemProperty()
                    .subscribe(viewModel::selectTrack);
                // Follows selection changes made by the view model (next/prev).
                viewModel.selectedTrackProperty()
                    .subscribe(track -> listView.getSelectionModel().select(track));
            })
            .build();
    }
}
