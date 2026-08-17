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
import com.sosuisha.domain.model.TrackMetadata;
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
    @DisplayName("絞り込みテキストを設定すると、アルバム一覧はアルバム名にそのテキストを含むアルバムだけになる")
    void setting_the_filter_text_narrows_the_album_list_to_albums_whose_name_contains_the_text() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var apple = new MusicFile(
            Path.of("a/one.mp3"), 100, new TrackMetadata("", "", "Apple", "X", "", "")
        );
        var banana = new MusicFile(
            Path.of("b/two.mp3"), 200, new TrackMetadata("", "", "Banana", "X", "", "")
        );
        viewModel.setFiles(List.of(apple, banana));

        viewModel.albumFilterProperty().set("App");

        assertEquals(List.of(new Album("Apple", "X", List.of(apple))), viewModel.getAlbums());
    }

    @Test
    @DisplayName("絞り込みは大文字小文字を無視して一致する")
    void the_filter_matches_ignoring_case() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var apple = new MusicFile(
            Path.of("a/one.mp3"), 100, new TrackMetadata("", "", "Apple", "X", "", "")
        );
        var banana = new MusicFile(
            Path.of("b/two.mp3"), 200, new TrackMetadata("", "", "Banana", "X", "", "")
        );
        viewModel.setFiles(List.of(apple, banana));

        viewModel.albumFilterProperty().set("aPP");

        assertEquals(List.of(new Album("Apple", "X", List.of(apple))), viewModel.getAlbums());
    }

    @Test
    @DisplayName("絞り込みテキストは、アルバムアーティスト名の部分一致でも絞り込む")
    void the_filter_text_also_narrows_by_partial_match_of_the_album_artist_name() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var apple = new MusicFile(
            Path.of("a/one.mp3"), 100, new TrackMetadata("", "", "Apple", "Xylo", "", "")
        );
        var banana = new MusicFile(
            Path.of("b/two.mp3"), 200, new TrackMetadata("", "", "Banana", "Zebra", "", "")
        );
        viewModel.setFiles(List.of(apple, banana));

        viewModel.albumFilterProperty().set("zeb");

        assertEquals(List.of(new Album("Banana", "Zebra", List.of(banana))), viewModel.getAlbums());
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
    @DisplayName("曲リストの行テキストは「トラック番号. 曲名 (アーティスト名)」である")
    void the_track_row_text_is_the_number_title_and_artist_of_the_track() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "Artist X", "Album A", "", "1", "")
        );

        assertEquals("1. Song One (Artist X)", viewModel.trackRowText(track));
    }

    @Test
    @DisplayName("曲のアーティスト名がアルバムアーティストと同じ場合、行テキストにアーティスト名は付かない")
    void the_artist_is_omitted_from_the_track_row_text_when_it_equals_the_album_artist() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SettingsAppModel(new NullSettingsRepository())
                ), new NullMusicPlayer(), _ -> {
                }
            );
        var track = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song One", "Artist X", "Album A", "Artist X", "1", "")
        );
        viewModel.selectAlbum(new Album("Album A", "Artist X", List.of(track)));

        assertEquals("1. Song One", viewModel.trackRowText(track));
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
