package com.sosuisha.presentation.screens.duplicatelist.components;

import java.util.Objects;

import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Component that shows the files of the selected duplicated group. Each row
 * shows the file path, the size, and a play button.
 */
public class ConfirmPanel {
    private static final double WIDTH = 300;
    private static final double ROW_WIDTH = 280;

    private ConfirmPanel() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the duplicate file list screen
     * @return panel that shows the files of the selected group
     * @throws NullPointerException if viewModel is null
     */
    public static VBox getRoot(DuplicateListViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        return VBoxBuilder
            .withChildren(
                ListViewBuilder.create(viewModel.getSelectedFiles())
                    .id("confirmFileList")
                    .cellFactory(_ -> new ListCell<>() {
                        @Override
                        protected void updateItem(MusicFile item, boolean empty) {
                            super.updateItem(item, empty);
                            setGraphic(
                                empty || item == null
                                    ? null
                                    : buildConfirmRow(viewModel, item)
                            );
                        }
                    })
                    .vGrowInVBox(Priority.ALWAYS)
                    .build()
            )
            .id("confirmPanel")
            .minWidth(WIDTH)
            .prefWidth(WIDTH)
            .maxWidth(WIDTH)
            .build();
    }

    private static VBox buildConfirmRow(DuplicateListViewModel viewModel, MusicFile item) {
        return VBoxBuilder
            .withChildren(
                LabelBuilder.create()
                    .text(item.path().toString())
                    .wrapText(true)
                    .build(),
                LabelBuilder.create()
                    .text(String.valueOf(item.size()))
                    .build(),
                ButtonBuilder.create()
                    .textPropertyApply(
                        text -> text.bind(
                            viewModel.playingFileProperty()
                                .map(playing -> item.equals(playing) ? "■" : "▶")
                                .orElse("▶")
                        )
                    )
                    .addStyleClass("play-button")
                    .onAction(_ -> viewModel.togglePlay(item))
                    .build()
            )
            .spacing(10)
            .addStyleClass("confirm-row")
            .prefWidth(ROW_WIDTH)
            .maxWidth(ROW_WIDTH)
            .build();
    }
}
