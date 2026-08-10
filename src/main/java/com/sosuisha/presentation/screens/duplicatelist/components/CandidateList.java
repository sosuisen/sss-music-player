package com.sosuisha.presentation.screens.duplicatelist.components;

import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.presentation.screens.duplicatelist.DuplicateListViewModel;

import io.github.sosuisen.jfxbuilder.controls.ListViewBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

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
            .cellFactory(
                CheckBoxListCell.forListView(
                    viewModel::checkedProperty,
                    new StringConverter<DuplicatedItems>() {
                        @Override
                        public String toString(DuplicatedItems item) {
                            return item.title();
                        }

                        @Override
                        public DuplicatedItems fromString(String string) {
                            throw new UnsupportedOperationException(
                                "the duplicate list is not editable"
                            );
                        }
                    }
                )
            )
            .apply(
                listView -> listView.getSelectionModel().selectedItemProperty()
                    .subscribe(item -> viewModel.select(item))
            )
            .build();
    }
}
