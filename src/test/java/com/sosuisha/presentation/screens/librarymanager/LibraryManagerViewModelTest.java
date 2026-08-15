package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.NullLibraryRepository;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryIndexer;
import com.sosuisha.domain.service.NullSettingsRepository;

import javafx.collections.ObservableList;

class LibraryManagerViewModelTest {
    @Test
    @DisplayName("受け取った初期リストをObservableListに格納する")
    void stores_received_initial_list_in_observable_list() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var files = List.of(
            new MusicFile(Path.of("first.mp3"), 100),
            new MusicFile(Path.of("second.m4a"), 200)
        );

        viewModel.setFiles(files);

        assertInstanceOf(ObservableList.class, viewModel.getFiles());
        assertEquals(files, viewModel.getFiles());
    }

    @Test
    @DisplayName("AppModelのファイルリストをそのまま返す")
    void returns_the_file_list_of_the_app_model() {
        var appModel = new MusicLibraryAppModel(
            new LibraryIndexer(new NullLibraryRepository()),
            new SettingsAppModel(new NullSettingsRepository())
        );
        var viewModel =
            new LibraryManagerViewModel(new WindowManager(), appModel, new NullMusicPlayer(), _ -> {
            });

        assertSame(appModel.getFiles(), viewModel.getFiles());
    }

    @Test
    @DisplayName("ソートキーがArtistのとき、アルバム行のテキストは「アーティスト名 - アルバム名」である")
    void the_album_row_text_is_artist_name_and_album_name_when_the_sort_key_is_artist() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        viewModel.sortKeyProperty().set(SortKey.ARTIST);

        var text = viewModel.albumRowText(new Album("Album A", "Artist X", List.of()));

        assertEquals("Artist X - Album A", text);
    }

    @Test
    @DisplayName("曲の再生中は、アルバムを選択しても曲一覧の先頭の曲が自動選択されない")
    void selecting_an_album_does_not_select_the_first_track_while_a_track_is_playing() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var playing = new MusicFile(Path.of("p/playing.mp3"), 100);
        viewModel.playTrack(playing);
        var trackOne = new MusicFile(Path.of("a/one.mp3"), 200);
        var trackTwo = new MusicFile(Path.of("a/two.mp3"), 300);

        viewModel.selectAlbum(new Album("Album A", "Artist X", List.of(trackOne, trackTwo)));

        assertEquals(playing, viewModel.selectedTrackProperty().get());
    }
}
