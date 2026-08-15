package com.sosuisha.presentation.screens.albumedit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.repository.SettingsRepository;

class AlbumEditViewModelTest {
    @Test
    @DisplayName("アルバムを選択すると、編集用のアルバム名とアルバムアーティストに選択中のアルバムの値が入る")
    void the_editable_album_name_and_album_artist_hold_the_values_of_the_selected_album() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new AlbumEditViewModel(appModel);

        appModel.selectAlbum(new Album("Album A", "Artist X", List.of()));

        assertEquals("Album A", viewModel.albumNameProperty().get());
        assertEquals("Artist X", viewModel.albumArtistProperty().get());
    }

    @Test
    @DisplayName("アルバムアーティストが空のアルバムを選択すると、編集用のアルバムアーティストに最初の曲のアーティスト名が自動入力される")
    void the_editable_album_artist_is_auto_filled_with_the_artist_of_the_first_track() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new AlbumEditViewModel(appModel);
        var first = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "Artist X", "Album A", "", "1", "")
        );

        appModel.selectAlbum(new Album("Album A", "", List.of(first)));

        assertEquals("Artist X", viewModel.albumArtistProperty().get());
    }

    @Test
    @DisplayName("編集用のアルバム名をアルバムの元の値と異なる値にすると、albumNameChangedがtrueになる")
    void album_name_changed_is_true_when_the_editable_album_name_differs_from_the_album() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new AlbumEditViewModel(appModel);
        appModel.selectAlbum(new Album("Album A", "Artist X", List.of()));

        viewModel.albumNameProperty().set("New Album");

        assertTrue(viewModel.albumNameChangedProperty().get());
    }

    @Test
    @DisplayName("アルバムアーティストが自動入力されたとき、albumArtistChangedはtrueである")
    void album_artist_changed_is_true_when_the_album_artist_is_auto_filled() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new AlbumEditViewModel(appModel);
        var first = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "Artist X", "Album A", "", "1", "")
        );

        appModel.selectAlbum(new Album("Album A", "", List.of(first)));

        assertTrue(viewModel.albumArtistChangedProperty().get());
    }

}
