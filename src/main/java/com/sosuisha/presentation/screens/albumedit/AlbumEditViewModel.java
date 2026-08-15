package com.sosuisha.presentation.screens.albumedit;

import java.util.Objects;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.service.TagWriter;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the album metadata edit screen.
 */
public class AlbumEditViewModel {
    private final MusicLibraryAppModel appModel;
    private final TagWriter tagWriter;
    private final StringProperty albumName = new SimpleStringProperty("");
    private final StringProperty albumArtist = new SimpleStringProperty("");
    private final BooleanProperty albumNameChanged = new SimpleBooleanProperty(false);
    private final BooleanProperty albumArtistChanged = new SimpleBooleanProperty(false);
    private final BooleanProperty libraryChanged = new SimpleBooleanProperty(false);

    /**
     * Creates the view model. The editable fields are reset to the values of
     * the selected album of the given app model whenever the selection
     * changes. An empty album artist is auto-filled with the artist of the
     * first track of the album.
     *
     * @param appModel application-wide state of the music library
     * @param tagWriter writer that saves the edited fields to the audio files
     * @throws NullPointerException if appModel or tagWriter is null
     */
    public AlbumEditViewModel(MusicLibraryAppModel appModel, TagWriter tagWriter) {
        this.appModel = Objects.requireNonNull(appModel, "appModel must not be null");
        this.tagWriter = Objects.requireNonNull(tagWriter, "tagWriter must not be null");
        appModel.selectedAlbumProperty().subscribe(album -> {
            albumName.set(album == null ? "" : album.name());
            albumArtist.set(album == null ? "" : albumArtistOf(album));
        });
        albumNameChanged.bind(
            Bindings.createBooleanBinding(
                () -> !albumName.get().equals(originalAlbumName(appModel)),
                albumName, appModel.selectedAlbumProperty()
            )
        );
        albumArtistChanged.bind(
            Bindings.createBooleanBinding(
                () -> !albumArtist.get().equals(originalAlbumArtist(appModel)),
                albumArtist, appModel.selectedAlbumProperty()
            )
        );
    }

    private static String originalAlbumName(MusicLibraryAppModel appModel) {
        var album = appModel.selectedAlbumProperty().get();
        return album == null ? "" : album.name();
    }

    private static String originalAlbumArtist(MusicLibraryAppModel appModel) {
        var album = appModel.selectedAlbumProperty().get();
        return album == null ? "" : album.artist();
    }

    private static String albumArtistOf(Album album) {
        if (!album.artist().isEmpty() || album.files().isEmpty()) { return album.artist(); }
        // An empty album artist is auto-filled with the artist of the first track.
        return album.files().getFirst().tag().artist();
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

    /**
     * Returns whether the editable album name differs from the name of the
     * selected album.
     *
     * @return read-only property that is true while the editable album name
     *     differs from the name of the selected album
     */
    public ReadOnlyBooleanProperty albumNameChangedProperty() {
        return albumNameChanged;
    }

    /**
     * Returns whether the editable album artist differs from the artist of
     * the selected album. An auto-filled album artist also counts as changed.
     *
     * @return read-only property that is true while the editable album artist
     *     differs from the artist of the selected album
     */
    public ReadOnlyBooleanProperty albumArtistChangedProperty() {
        return albumArtistChanged;
    }

    /**
     * Saves the edited album name and album artist to the tags of all tracks
     * of the selected album and marks the library as changed. Does nothing
     * when no album is selected.
     */
    public void save() {
        var album = appModel.selectedAlbumProperty().get();
        if (album == null) { return; }
        for (var file : album.files()) {
            tagWriter.writeAlbumTag(file.path(), albumName.get(), albumArtist.get());
        }
        libraryChanged.set(true);
    }

    /**
     * Tells that the edit window has been opened. The library changed flag is
     * cleared for the new edit session.
     */
    public void windowOpened() {
        libraryChanged.set(false);
    }

    /**
     * Tells that the edit window has been closed. The library is rescanned
     * when a save of this session has changed its audio files.
     */
    public void windowClosed() {
        if (libraryChanged.get()) {
            appModel.rescan();
        }
    }

    /**
     * Returns whether a save of this screen has changed the audio files of
     * the library.
     *
     * @return read-only property that is true while the library is marked as
     *     changed
     */
    public ReadOnlyBooleanProperty libraryChangedProperty() {
        return libraryChanged;
    }

}
