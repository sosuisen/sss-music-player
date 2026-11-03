package com.sosuisha.presentation.screens.librarymanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.exception.FolderOpenException;
import com.sosuisha.domain.model.Album;
import com.sosuisha.presentation.screens.librarymanager.AlbumListItem.AlbumItem;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.PlaybackPosition;
import com.sosuisha.domain.model.RepeatMode;
import com.sosuisha.domain.model.TrackMetadata;
import com.sosuisha.domain.repository.NullLibraryRepository;
import com.sosuisha.domain.service.MusicPlayer;
import com.sosuisha.domain.service.NullMusicPlayer;
import com.sosuisha.domain.repository.NullSettingsRepository;
import com.sosuisha.presentation.WindowManager;
import com.sosuisha.domain.repository.InMemoryFavoriteRepository;
import com.sosuisha.presentation.appmodel.FavoritesAppModel;
import com.sosuisha.presentation.appmodel.MusicLibraryAppModel;
import com.sosuisha.presentation.appmodel.SettingsAppModel;
import com.sosuisha.service.LibraryIndexer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

class LibraryManagerViewModelTest {
    @Test
    @DisplayName("リピートモードをトグルすると、SettingsAppModelのリピートモードが更新される")
    void toggling_the_repeat_mode_updates_the_repeat_mode_of_the_settings_app_model() {
        var settingsAppModel = new SettingsAppModel(new NullSettingsRepository());
        var viewModel = new LibraryManagerViewModel(
            new WindowManager(),
            new MusicLibraryAppModel(
                new LibraryIndexer(new NullLibraryRepository()),
                new SimpleObjectProperty<>()
            ),
            settingsAppModel,
            new FavoritesAppModel(new InMemoryFavoriteRepository()),
            new NullMusicPlayer(),
            _ -> {
            }
        );

        viewModel.toggleRepeatMode();

        assertEquals(RepeatMode.ONE, settingsAppModel.repeatModeProperty().get());
    }

    @Test
    @DisplayName("受け取った初期リストをObservableListに格納する")
    void stores_received_initial_list_in_observable_list() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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
            new SimpleObjectProperty<>()
        );
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), appModel, new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()),
                new NullMusicPlayer(), _ -> {
                }
            );

        assertSame(appModel.getFiles(), viewModel.getFiles());
    }

    @Test
    @DisplayName("絞り込みテキストを設定すると、アルバム一覧はアルバム名にそのテキストを含むアルバムだけになる")
    void setting_the_filter_text_narrows_the_album_list_to_albums_whose_name_contains_the_text() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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

        assertEquals(
            List.of(new AlbumItem(new Album("Apple", "X", List.of(apple)))),
            viewModel.getListItems()
        );
    }

    @Test
    @DisplayName("絞り込みは大文字小文字を無視して一致する")
    void the_filter_matches_ignoring_case() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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

        assertEquals(
            List.of(new AlbumItem(new Album("Apple", "X", List.of(apple)))),
            viewModel.getListItems()
        );
    }

    @Test
    @DisplayName("絞り込みテキストは、アルバムアーティスト名の部分一致でも絞り込む")
    void the_filter_text_also_narrows_by_partial_match_of_the_album_artist_name() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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

        assertEquals(
            List.of(new AlbumItem(new Album("Banana", "Zebra", List.of(banana)))),
            viewModel.getListItems()
        );
    }

    @Test
    @DisplayName("ソートキーがArtistのとき、アルバム行のテキストは「アーティスト名 - アルバム名」である")
    void the_album_row_text_is_artist_name_and_album_name_when_the_sort_key_is_artist() {
        var viewModel =
            new LibraryManagerViewModel(
                new WindowManager(), new MusicLibraryAppModel(
                    new LibraryIndexer(new NullLibraryRepository()),
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
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
                    new SimpleObjectProperty<>()
                ), new SettingsAppModel(new NullSettingsRepository()),
                new FavoritesAppModel(new InMemoryFavoriteRepository()), new NullMusicPlayer(),
                _ -> {
                }
            );
        var playing = new MusicFile(Path.of("p/playing.mp3"), 100);
        viewModel.playTrack(playing);
        var trackOne = new MusicFile(Path.of("a/one.mp3"), 200);
        var trackTwo = new MusicFile(Path.of("a/two.mp3"), 300);

        viewModel.selectAlbum(new Album("Album A", "Artist X", List.of(trackOne, trackTwo)));

        assertEquals(playing, viewModel.selectedTrackProperty().get());
    }

    @Test
    @DisplayName("フォルダを開けないと、エラーメッセージプロパティに失敗の理由がセットされる")
    void a_failed_folder_open_sets_the_reason_to_the_error_message_property() {
        var viewModel = new LibraryManagerViewModel(
            new WindowManager(),
            new MusicLibraryAppModel(
                new LibraryIndexer(new NullLibraryRepository()),
                new SimpleObjectProperty<>()
            ),
            new SettingsAppModel(new NullSettingsRepository()),
            new FavoritesAppModel(new InMemoryFavoriteRepository()),
            new NullMusicPlayer(),
            folder -> {
                throw new FolderOpenException("Could not open the folder: " + folder, null);
            }
        );
        viewModel.selectTrack(new MusicFile(Path.of("a/one.mp3"), 100));

        viewModel.openTrackFolder();

        assertEquals(
            "Could not open the folder: " + Path.of("a"), viewModel.errorMessageProperty().get()
        );
    }

    @Test
    @DisplayName("再生中に曲の選択が解除されてから曲が終わっても、RepeatMode.ONEでは同じ曲を再生し直す")
    void the_finished_track_is_played_again_in_repeat_one_even_after_the_selection_was_cleared_while_playing() {
        var onFinished = new AtomicReference<@Nullable Runnable>();
        var playedPaths = new ArrayList<Path>();
        MusicPlayer recordingPlayer = new NullMusicPlayer() {
            @Override
            public void play(Path path) {
                playedPaths.add(path);
            }

            @Override
            public void setOnFinished(Runnable callback) {
                onFinished.set(callback);
            }
        };
        var settingsAppModel = new SettingsAppModel(new NullSettingsRepository());
        var viewModel = new LibraryManagerViewModel(
            new WindowManager(),
            new MusicLibraryAppModel(
                new LibraryIndexer(new NullLibraryRepository()),
                new SimpleObjectProperty<>()
            ),
            settingsAppModel,
            new FavoritesAppModel(new InMemoryFavoriteRepository()),
            recordingPlayer,
            _ -> {
            }
        );
        settingsAppModel.repeatModeProperty().set(RepeatMode.ONE);
        var playing = new MusicFile(Path.of("p/playing.mp3"), 100);
        viewModel.playTrack(playing);
        viewModel.selectTrack(null);

        Objects.requireNonNull(onFinished.get()).run();

        assertEquals(List.of(playing.path(), playing.path()), playedPaths);
    }

    @Test
    @DisplayName("再生の失敗が通知されると、プレイヤーの状態は停止に戻り、エラーメッセージにファイル名と理由を出す")
    void a_playback_failure_stops_the_player_and_shows_the_file_name_and_the_reason() {
        var onError = new AtomicReference<@Nullable Consumer<String>>();
        MusicPlayer failingPlayer = new NullMusicPlayer() {
            @Override
            public void setOnError(Consumer<String> callback) {
                onError.set(callback);
            }
        };
        var viewModel = viewModelWith(failingPlayer);
        viewModel.playTrack(new MusicFile(Path.of("a/one.m4a"), 100));

        Objects.requireNonNull(onError.get()).accept("ERROR_MEDIA_INVALID");

        assertEquals(PlayerState.STOPPED, viewModel.playerStateProperty().get());
        assertEquals(
            "Cannot play one.m4a: ERROR_MEDIA_INVALID", viewModel.errorMessageProperty().get()
        );
    }

    @Test
    @DisplayName("再生を始めた時点で失敗が通知されても、プレイヤーの状態は停止になる")
    void a_failure_reported_when_playback_starts_leaves_the_player_stopped() {
        MusicPlayer failingPlayer = new NullMusicPlayer() {
            private Consumer<String> onError = _ -> {
            };

            @Override
            public void play(Path path) {
                onError.accept("MEDIA_UNAVAILABLE");
            }

            @Override
            public void setOnError(Consumer<String> callback) {
                onError = callback;
            }
        };
        var viewModel = viewModelWith(failingPlayer);

        viewModel.playTrack(new MusicFile(Path.of("a/missing.mp3"), 100));

        assertEquals(PlayerState.STOPPED, viewModel.playerStateProperty().get());
    }

    @Test
    @DisplayName("次の再生が始まると、エラーメッセージは消える")
    void the_error_message_is_cleared_when_the_next_playback_starts() {
        var onError = new AtomicReference<@Nullable Consumer<String>>();
        MusicPlayer failingPlayer = new NullMusicPlayer() {
            @Override
            public void setOnError(Consumer<String> callback) {
                onError.set(callback);
            }
        };
        var viewModel = viewModelWith(failingPlayer);
        viewModel.playTrack(new MusicFile(Path.of("a/one.m4a"), 100));
        Objects.requireNonNull(onError.get()).accept("ERROR_MEDIA_INVALID");

        viewModel.playTrack(new MusicFile(Path.of("a/two.mp3"), 100));

        assertNull(viewModel.errorMessageProperty().get());
    }

    @Test
    @DisplayName("再生位置の通知を受けると、再生位置と曲の長さを公開する")
    void a_progress_report_publishes_the_position_and_the_length() {
        var onProgress = new AtomicReference<@Nullable Consumer<PlaybackPosition>>();
        var viewModel = viewModelWith(reportingPlayer(onProgress));
        viewModel.playTrack(new MusicFile(Path.of("a/one.mp3"), 100));

        Objects.requireNonNull(onProgress.get())
            .accept(new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245)));

        assertEquals(
            new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245)),
            viewModel.playbackPositionProperty().get()
        );
    }

    @Test
    @DisplayName("停止すると、再生位置はなくなる")
    void stopping_clears_the_playback_position() {
        var onProgress = new AtomicReference<@Nullable Consumer<PlaybackPosition>>();
        var viewModel = viewModelWith(reportingPlayer(onProgress));
        viewModel.playTrack(new MusicFile(Path.of("a/one.mp3"), 100));
        Objects.requireNonNull(onProgress.get())
            .accept(new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245)));

        viewModel.stopPlayback();

        assertNull(viewModel.playbackPositionProperty().get());
    }

    @Test
    @DisplayName("別の曲を再生し始めると、新しい曲の通知が来るまで、再生位置はなくなる")
    void starting_another_track_clears_the_playback_position() {
        var onProgress = new AtomicReference<@Nullable Consumer<PlaybackPosition>>();
        var viewModel = viewModelWith(reportingPlayer(onProgress));
        viewModel.playTrack(new MusicFile(Path.of("a/one.mp3"), 100));
        Objects.requireNonNull(onProgress.get())
            .accept(new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245)));

        viewModel.playTrack(new MusicFile(Path.of("a/two.mp3"), 100));

        assertNull(viewModel.playbackPositionProperty().get());
    }

    @Test
    @DisplayName("再生中に次の曲・前の曲へ移ると、再生位置はなくなる")
    void moving_to_another_track_while_playing_clears_the_playback_position() {
        var onProgress = new AtomicReference<@Nullable Consumer<PlaybackPosition>>();
        var viewModel = viewModelWith(reportingPlayer(onProgress));
        var trackOne = new MusicFile(Path.of("a/one.mp3"), 200);
        var trackTwo = new MusicFile(Path.of("a/two.mp3"), 300);
        viewModel.selectAlbum(new Album("Album A", "Artist X", List.of(trackOne, trackTwo)));
        viewModel.playTrack(trackOne);
        Objects.requireNonNull(onProgress.get())
            .accept(new PlaybackPosition(Duration.ofSeconds(83), Duration.ofSeconds(245)));

        viewModel.nextTrack();

        assertNull(viewModel.playbackPositionProperty().get());
    }

    // A player that hands its progress callback to the test.
    private static MusicPlayer reportingPlayer(
        AtomicReference<@Nullable Consumer<PlaybackPosition>> onProgress) {
        return new NullMusicPlayer() {
            @Override
            public void setOnProgress(Consumer<PlaybackPosition> callback) {
                onProgress.set(callback);
            }
        };
    }

    private static LibraryManagerViewModel viewModelWith(MusicPlayer musicPlayer) {
        return new LibraryManagerViewModel(
            new WindowManager(),
            new MusicLibraryAppModel(
                new LibraryIndexer(new NullLibraryRepository()),
                new SimpleObjectProperty<>()
            ),
            new SettingsAppModel(new NullSettingsRepository()),
            new FavoritesAppModel(new InMemoryFavoriteRepository()),
            musicPlayer,
            _ -> {
            }
        );
    }
}
