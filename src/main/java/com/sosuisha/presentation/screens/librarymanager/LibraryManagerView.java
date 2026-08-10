package com.sosuisha.presentation.screens.librarymanager;

import java.util.Objects;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuBarBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuItemBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;

/**
 * View for the library manager screen.
 */
public class LibraryManagerView implements View {
    private static final String TITLE = "Library Manager";

    private final LibraryManagerViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the library manager screen
     * @throws NullPointerException if viewModel is null
     */
    public LibraryManagerView(LibraryManagerViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
        this.scene = buildSceneGraph();
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
                        ListViewBuilder.create(viewModel.getFiles())
                            .id("fileList")
                            .cellFactory(_ -> new ListCell<>() {
                                @Override
                                protected void updateItem(MusicFile item, boolean empty) {
                                    super.updateItem(item, empty);
                                    setText(empty || item == null ? null : item.path().toString());
                                }
                            })
                            .build()
                    )
                    .build()
            )
            .build();
    }
}
