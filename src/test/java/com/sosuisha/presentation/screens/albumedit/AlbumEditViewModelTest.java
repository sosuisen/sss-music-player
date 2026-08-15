package com.sosuisha.presentation.screens.albumedit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Album;
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

}
