package com.sosuisha.presentation.screens.duplicatelist;

import java.util.Objects;


import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.service.DuplicateDetector;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the duplicate file list screen.
 */
public class DuplicateListViewModel {
    private final MusicLibraryAppModel appModel;

    private final ObservableList<DuplicatedItems> duplicatedItems =
        FXCollections.observableArrayList();

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the music library
     * @throws NullPointerException if appModel is null
     */
    public DuplicateListViewModel(MusicLibraryAppModel appModel) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
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
     * Returns the list of duplicated items.
     *
     * @return observable list of duplicated items
     */
    public ObservableList<DuplicatedItems> getDuplicatedItems() {
        return duplicatedItems;
    }
}
