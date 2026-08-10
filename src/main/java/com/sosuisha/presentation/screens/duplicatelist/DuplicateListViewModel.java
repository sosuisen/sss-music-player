package com.sosuisha.presentation.screens.duplicatelist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.DuplicateDetector;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.service.FilenameAndSizeDuplicateDetector;
import com.sosuisha.service.FilenameDuplicateDetector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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

    private final ObservableList<DuplicatedItems> duplicatedItems =
        FXCollections.observableArrayList();
    private final ObjectProperty<DuplicatedItems> selectedItem = new SimpleObjectProperty<>();
    private final ObservableList<MusicFile> selectedFiles = FXCollections.observableArrayList();
    private final ObjectProperty<MusicFile> playingFile = new SimpleObjectProperty<>();
    private final Map<DuplicatedItems, BooleanProperty> checkedItems = new HashMap<>();

    /**
     * Creates the view model.
     *
     * @param appModel application-wide state of the music library
     * @param musicPlayer player used to play audio files
     * @throws NullPointerException if appModel or musicPlayer is null
     */
    public DuplicateListViewModel(MusicLibraryAppModel appModel, MusicPlayer musicPlayer) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.musicPlayer = Objects.requireNonNull(musicPlayer, "musicPlayer must not be null");
        selectedItem.subscribe(
            item -> selectedFiles.setAll(item == null ? List.of() : item.files())
        );
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
        return checkedItems.computeIfAbsent(item, _ -> new SimpleBooleanProperty(false));
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
     * states are cleared.
     *
     * @param detector duplicate detector to call
     * @throws NullPointerException if detector is null
     */
    public void detect(DuplicateDetector detector) {
        Objects.requireNonNull(detector, "detector must not be null");
        checkedItems.clear();
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
