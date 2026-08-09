package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;

class LibraryManagerViewModelTest {
    @Test
    @DisplayName("受け取った初期リストをObservableListに格納する")
    void stores_received_initial_list_in_observable_list() {
        var viewModel = new LibraryManagerViewModel();
        var files = List.of(Path.of("first.mp3"), Path.of("second.m4a"));

        viewModel.setFiles(files);

        assertInstanceOf(ObservableList.class, viewModel.getFiles());
        assertEquals(files, viewModel.getFiles());
    }
}
