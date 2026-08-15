package com.sosuisha.presentation.screens.albumedit;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @DisplayName("アルバムを選択すると、編集用の年に最初の曲のタグの年が自動入力される")
    void the_editable_year_is_auto_filled_with_the_year_of_the_first_track() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new SettingsRepository())
        );
        var viewModel = new AlbumEditViewModel(appModel);
        var first = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "", "Album A", "Artist X", "1", "2001")
        );

        appModel.selectAlbum(new Album("Album A", "Artist X", List.of(first)));

        assertEquals("2001", viewModel.albumYearProperty().get());
    }
}
