package com.sosuisha.presentation.screens.librarymanager;

import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuBarBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuItemBuilder;
import io.github.sosuisen.jfxbuilder.controls.SplitPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.StageBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * View for the library manager screen.
 */
public class LibraryManagerView implements View {
    private static final String TITLE = "Library Manager";
    private static final double SCANNING_DIALOG_WIDTH = 600;
    private static final double SCANNING_DIALOG_PADDING = 20;

    private final LibraryManagerViewModel viewModel;
    private final Scene scene;
    private Stage scanningDialog;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the library manager screen
     * @throws NullPointerException if viewModel is null
     */
    public LibraryManagerView(LibraryManagerViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.scene = buildSceneGraph();
        viewModel.scanningProperty().subscribe(this::updateScanningDialog);
    }

    private void updateScanningDialog(boolean scanning) {
        if (scanning) {
            if (scanningDialog == null) {
                scanningDialog = buildScanningDialog();
            }
            scanningDialog.show();
        } else if (scanningDialog != null) {
            scanningDialog.close();
        }
    }

    private Stage buildScanningDialog() {
        return StageBuilder.create()
            .title("Scanning")
            .width(SCANNING_DIALOG_WIDTH)
            .scene(
                SceneBuilder
                    .withRoot(
                        VBoxBuilder
                            .withChildren(
                                LabelBuilder.create()
                                    .text("Scanning the music library...")
                                    .build(),
                                LabelBuilder.create()
                                    .id("scanningFile")
                                    .wrapText(true)
                                    .textPropertyApply(
                                        prop -> prop.bind(viewModel.scanningFileProperty())
                                    )
                                    .build()
                            )
                            .padding(new Insets(SCANNING_DIALOG_PADDING))
                            .build()
                    )
                    .build()
            )
            .apply(stage -> {
                stage.initModality(Modality.APPLICATION_MODAL);
                if (scene.getWindow() != null) {
                    // The owner keeps the dialog in front of the main window.
                    stage.initOwner(scene.getWindow());
                }
            })
            .build();
    }

    private ListView<Album> buildAlbumList() {
        return ListViewBuilder.create(viewModel.getAlbums())
            .id("albumList")
            .cellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(Album item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.name() + " - " + item.artist());
                }
            })
            .apply(
                listView -> listView.getSelectionModel().selectedItemProperty()
                    .subscribe(viewModel::selectAlbum)
            )
            .build();
    }

    private ListView<MusicFile> buildTrackList() {
        return ListViewBuilder.create(viewModel.getSelectedTracks())
            .id("trackList")
            .cellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(MusicFile item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : trackText(item));
                }
            })
            .build();
    }

    private static String trackText(MusicFile file) {
        var number = file.tag().trackNumber();
        var title = file.tag().title().isEmpty()
            ? file.path().getFileName().toString()
            : file.tag().title();
        return number.isEmpty() ? title : number + ". " + title;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    private Scene buildSceneGraph() {
        return SceneBuilder
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        MenuBarBuilder
                            .withMenus(
                                MenuBuilder
                                    .withItems(
                                        MenuItemBuilder.create()
                                            .text("Rescan")
                                            .onAction(_ -> viewModel.rescan())
                                            .build(),
                                        MenuItemBuilder.create()
                                            .text("Remove duplicate files...")
                                            .onAction(_ -> viewModel.openDuplicateListWindow())
                                            .build(),
                                        MenuItemBuilder.create()
                                            .text("Settings...")
                                            .onAction(_ -> viewModel.openSettingsWindow())
                                            .build()
                                    )
                                    .text("File")
                                    .build()
                            )
                            .build(),
                        SplitPaneBuilder
                            .withItems(buildAlbumList(), buildTrackList())
                            .vGrowInVBox(Priority.ALWAYS)
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
