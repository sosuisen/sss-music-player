package com.sosuisha.presentation.screens.duplicatelist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.DuplicateDetector;
import com.sosuisha.domain.service.FolderOpener;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.service.DuplicateFileMover;
import com.sosuisha.service.FilenameAndSizeDuplicateDetector;
import com.sosuisha.service.FilenameDuplicateDetector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the duplicate file list screen.
 */
public class DuplicateListViewModel {
    private final MusicLibraryAppModel appModel;
    private final MusicPlayer musicPlayer;
    private final DuplicateFileMover duplicateFileMover;
    private final FolderOpener folderOpener;

    private final ObservableList<DuplicatedItems> duplicatedItems =
        FXCollections.observableArrayList();
    private final ObjectProperty<DuplicatedItems> selectedItem = new SimpleObjectProperty<>();
    private final ObservableList<MusicFile> selectedFiles = FXCollections.observableArrayList();
    private final ObjectProperty<MusicFile> playingFile = new SimpleObjectProperty<>();
    private final Map<DuplicatedItems, BooleanProperty> checkedItems = new HashMap<>();
    private final BooleanProperty anyChecked = new SimpleBooleanProperty(false);
    private DuplicateDetector lastDetector;

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the music library
     * @param musicPlayer player used to play audio files
     * @param duplicateFileMover mover that moves duplicated files out of the library
     * @param folderOpener opener that shows a folder in the file manager
     * @throws NullPointerException if appModel, musicPlayer, duplicateFileMover or
     *             folderOpener is null
     */
    public DuplicateListViewModel(MusicLibraryAppModel appModel, MusicPlayer musicPlayer,
        DuplicateFileMover duplicateFileMover, FolderOpener folderOpener) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.musicPlayer = Objects.requireNonNull(musicPlayer, "musicPlayer must not be null");
        this.duplicateFileMover =
            Objects.requireNonNull(duplicateFileMover, "duplicateFileMover must not be null");
        this.folderOpener = Objects.requireNonNull(folderOpener, "folderOpener must not be null");
        selectedItem.subscribe(
            item -> selectedFiles.setAll(item == null ? List.of() : item.files())
        );
        appModel.getFiles().subscribe(() -> {
            if (lastDetector != null) {
                detect(lastDetector);
            }
        });
    }

    /**
     * Moves the duplicated files of the checked groups out of the library and
     * rescans the library.
     *
     * @throws UncheckedIOException if a file cannot be moved
     */
    public void removeCheckedDuplicates() {
        var checkedGroups = duplicatedItems.stream()
            .filter(item -> checkedProperty(item).get())
            .toList();
        try {
            duplicateFileMover.moveDuplicates(checkedGroups);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        appModel.rescan();
    }

    /**
     * Plays the given audio file. The file becomes the playing file.
     *
     * @param file audio file to play
     * @throws NullPointerException if file is null
     */
    public void play(MusicFile file) {
        Objects.requireNonNull(file, "file must not be null");
        musicPlayer.play(file.path());
        playingFile.set(file);
    }

    /**
     * Stops the audio file that is currently playing. No file is the playing
     * file afterwards.
     */
    public void stop() {
        musicPlayer.stop();
        playingFile.set(null);
    }

    /**
     * Opens the folder that contains the given audio file.
     *
     * @param file audio file whose folder is opened
     * @throws NullPointerException if file is null
     */
    public void openFolder(MusicFile file) {
        Objects.requireNonNull(file, "file must not be null");
        folderOpener.open(file.path().getParent());
    }

    /**
     * Plays the given audio file, or stops it when it is the playing file.
     *
     * @param file audio file to play or stop
     * @throws NullPointerException if file is null
     */
    public void togglePlay(MusicFile file) {
        Objects.requireNonNull(file, "file must not be null");
        if (file.equals(playingFile.get())) {
            stop();
        } else {
            play(file);
        }
    }

    /**
     * Returns the audio file that is currently playing, or null when nothing
     * is playing.
     *
     * @return object property of the playing audio file
     */
    public ObjectProperty<MusicFile> playingFileProperty() {
        return playingFile;
    }

    /**
     * Returns the checked state of the given duplicated group. The state is
     * kept per group in this view model.
     *
     * @param item duplicated group
     * @return boolean property of the checked state of the group
     * @throws NullPointerException if item is null
     */
    public BooleanProperty checkedProperty(DuplicatedItems item) {
        Objects.requireNonNull(item, "item must not be null");
        return checkedItems.computeIfAbsent(item, _ -> {
            var property = new SimpleBooleanProperty(false);
            property.subscribe(_ -> updateAnyChecked());
            return property;
        });
    }

    /**
     * Returns whether at least one duplicated group is checked.
     *
     * @return read-only boolean property that is true when any group is checked
     */
    public ReadOnlyBooleanProperty anyCheckedProperty() {
        return anyChecked;
    }

    /**
     * Checks all duplicated groups, or unchecks all of them when at least one
     * group is checked.
     */
    public void toggleAllChecks() {
        var newValue = !anyChecked.get();
        for (var item : duplicatedItems) {
            checkedProperty(item).set(newValue);
        }
    }

    private void updateAnyChecked() {
        anyChecked.set(checkedItems.values().stream().anyMatch(BooleanProperty::get));
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
     * observable list instance is kept; its contents are replaced. All checked
     * states are cleared. The detector is called again whenever the files of
     * the music library change.
     *
     * @param detector duplicate detector to call
     * @throws NullPointerException if detector is null
     */
    public void detect(DuplicateDetector detector) {
        Objects.requireNonNull(detector, "detector must not be null");
        lastDetector = detector;
        checkedItems.clear();
        updateAnyChecked();
        duplicatedItems.setAll(detector.detect());
    }

    /**
     * Detects duplicated files by file name and stores the result.
     */
    public void detectByFilename() {
        detect(new FilenameDuplicateDetector(appModel.getFiles()));
    }

    /**
     * Detects duplicated files by file name and size and stores the result.
     */
    public void detectByFilenameAndSize() {
        detect(new FilenameAndSizeDuplicateDetector(appModel.getFiles()));
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
