package com.sosuisha.presentation.screens.duplicatelist;

import java.util.List;
import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.DuplicateDetector;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.service.FilenameDuplicateDetector;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the duplicate file list screen.
 */
public class DuplicateListViewModel {
    private final MusicLibraryAppModel appModel;

    private final ObservableList<DuplicatedItems> duplicatedItems =
        FXCollections.observableArrayList();
    private final ObjectProperty<DuplicatedItems> selectedItem = new SimpleObjectProperty<>();
    private final ObservableList<MusicFile> selectedFiles = FXCollections.observableArrayList();

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the music library
     * @throws NullPointerException if appModel is null
     */
    public DuplicateListViewModel(MusicLibraryAppModel appModel) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        selectedItem.subscribe(
            item -> selectedFiles.setAll(item == null ? List.of() : item.files())
        );
    }

    /**
     * Returns the selected duplicated group. The files of the group are
     * published through {@link #getSelectedFiles()}.
     *
     * @return object property of the selected duplicated group
     */
    public ObjectProperty<DuplicatedItems> selectedItemProperty() {
        return selectedItem;
    }

    /**
     * Returns the files of the selected duplicated group. The list is empty
     * when no group is selected.
     *
     * @return observable list of the files of the selected group
     */
    public ObservableList<MusicFile> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * Calls the given detector and stores the detected duplicated items. The
     * observable list instance is kept; its contents are replaced.
     *
     * @param detector duplicate detector to call
     * @throws NullPointerException if detector is null
     */
    public void detect(DuplicateDetector detector) {
        Objects.requireNonNull(detector, "detector must not be null");
        duplicatedItems.setAll(detector.detect());
    }

    /**
     * Detects duplicated files by file name and stores the result.
     */
    public void detectByFilename() {
        detect(new FilenameDuplicateDetector(appModel.getFiles()));
    }

    /**
     * Returns the list of duplicated items.
     *
     * @return observable list of duplicated items
     */
    public ObservableList<DuplicatedItems> getDuplicatedItems() {
        return duplicatedItems;
    }
}
