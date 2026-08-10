package com.sosuisha.presentation.screens.duplicatelist.components;

import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;

import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Component that lists the duplicated groups. Selecting a group publishes it
 * to the view model.
 */
public class CandidateList {
    private CandidateList() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the duplicate file list screen
     * @return list view of the duplicated groups
     * @throws NullPointerException if viewModel is null
     */
    public static ListView<DuplicatedItems> getRoot(DuplicateListViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        return ListViewBuilder.create(viewModel.getDuplicatedItems())
            .id("duplicateList")
            .cellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(DuplicatedItems item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.title());
                }
            })
            .apply(
                listView -> viewModel.selectedItemProperty()
                    .bind(listView.getSelectionModel().selectedItemProperty())
            )
            .build();
    }
}
