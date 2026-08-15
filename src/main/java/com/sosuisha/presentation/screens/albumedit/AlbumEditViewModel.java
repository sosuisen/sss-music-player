package com.sosuisha.presentation.screens.albumedit;

import java.util.Objects;

import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the album metadata edit screen.
 */
public class AlbumEditViewModel {
    private final StringProperty albumName = new SimpleStringProperty("");
    private final StringProperty albumArtist = new SimpleStringProperty("");

    /**
     * Creates the view model. The editable fields are reset to the values of
     * the selected album of the given app model whenever the selection
     * changes.
     *
     * @param appModel application-wide state of the music library
     * @throws NullPointerException if appModel is null
     */
    public AlbumEditViewModel(MusicLibraryAppModel appModel) {
        Objects.requireNonNull(appModel, "appModel must not be null");
        appModel.selectedAlbumProperty().subscribe(album -> {
            albumName.set(album == null ? "" : album.name());
            albumArtist.set(album == null ? "" : album.artist());
        });
    }

    /**
     * Returns the editable album name.
     *
     * @return string property of the album name
     */
    public StringProperty albumNameProperty() {
        return albumName;
    }

    /**
     * Returns the editable album artist.
     *
     * @return string property of the album artist
     */
    public StringProperty albumArtistProperty() {
        return albumArtist;
    }
}
