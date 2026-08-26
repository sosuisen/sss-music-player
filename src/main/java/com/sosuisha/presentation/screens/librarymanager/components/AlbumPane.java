package com.sosuisha.presentation.screens.librarymanager.components;

import java.util.Objects;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import com.sosuisha.domain.model.Album;
import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;
import com.sosuisha.presentation.screens.librarymanager.SortKey;

import atlantafx.base.controls.CustomTextField;
import io.github.sosuisen.jfxbuilder.controls.ComboBoxBuilder;
import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import io.github.sosuisen.jfxbuilder.graphics.HBoxBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

/**
 * Component that shows the album list with its toolbar: the sort key combo
 * box and the album filter field.
 */
public class AlbumPane {
    private AlbumPane() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the library manager screen
     * @return pane holding the toolbar and the album list
     * @throws NullPointerException if viewModel is null
     */
    public static VBox getRoot(LibraryManagerViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        var albumList = buildAlbumList(viewModel);
        VBox.setVgrow(albumList, Priority.ALWAYS);
        return VBoxBuilder
            .withChildren(buildToolbar(viewModel), albumList)
            .build();
    }

    private static HBox buildToolbar(LibraryManagerViewModel viewModel) {
        return HBoxBuilder
            .withChildren(
                ComboBoxBuilder.<SortKey>create()
                    .id("sortKey")
                    .apply(comboBox -> comboBox.getItems().addAll(SortKey.ALBUM, SortKey.ARTIST))
                    .valuePropertyApply(prop -> prop.bindBidirectional(viewModel.sortKeyProperty()))
                    .build(),
                buildFilterField(viewModel)
            )
            .build();
    }

    private static CustomTextField buildFilterField(LibraryManagerViewModel viewModel) {
        var field = new CustomTextField();
        field.setId("albumFilter");
        field.setPromptText("Search");
        field.textProperty().bindBidirectional(viewModel.albumFilterProperty());
        field.setLeft(new FontIcon(Material2MZ.SEARCH));
        var clearIcon = new FontIcon(Material2AL.CLEAR);
        clearIcon.setId("clearAlbumFilter");
        clearIcon.setCursor(Cursor.HAND);
        clearIcon.setOnMouseClicked(_ -> viewModel.clearAlbumFilter());
        field.setRight(clearIcon);
        return field;
    }

    private static ListView<Album> buildAlbumList(LibraryManagerViewModel viewModel) {
        return ListViewBuilder.create(viewModel.getAlbums())
            .id("albumList")
            .cellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(@Nullable Album item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : viewModel.albumRowText(item));
                }
            })
            .apply(
                listView -> listView.getSelectionModel().selectedItemProperty()
                    .subscribe(viewModel::selectAlbum)
            )
            .build();
    }
}
