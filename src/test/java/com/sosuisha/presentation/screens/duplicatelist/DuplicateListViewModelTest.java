package com.sosuisha.presentation.screens.duplicatelist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.service.LibraryScanner;

import javafx.collections.ObservableList;

class DuplicateListViewModelTest {
    @Test
    @DisplayName("detectで重複判定関数を呼び出して、重複リストをObservableListに格納する")
    void detect_stores_duplicated_items_in_observable_list() {
        var items = List.of(
            new DuplicatedItems(
                "first.mp3",
                List.of(Path.of("a/first.mp3"), Path.of("b/first.mp3"))
            )
        );
        var viewModel = new DuplicateListViewModel(new MusicLibraryAppModel(new LibraryScanner()));

        viewModel.detect(() -> items);

        assertInstanceOf(ObservableList.class, viewModel.getDuplicatedItems());
        assertEquals(items, viewModel.getDuplicatedItems());
    }
}
