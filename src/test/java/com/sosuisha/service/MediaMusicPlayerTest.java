package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;

import javafx.scene.media.Media;
import org.jspecify.annotations.Nullable;

// Media and MediaPlayer use the JavaFX toolkit in their background threads.
// Without the toolkit, those threads fail, and the test JVM may exit badly.
// The extension starts the toolkit before each test.
@ExtendWith(ApplicationExtension.class)
class MediaMusicPlayerTest {
    @Test
    @DisplayName("濁点を結合文字で書いた（NFDの）フォルダー名にある曲のMediaを作れる")
    void creates_the_media_of_a_track_in_a_folder_whose_name_uses_a_combining_voiced_mark()
        throws IOException {
        var tempDir = createFolderInTarget("nfd");
        // "ク" followed by the combining voiced mark. It looks like "グ", but
        // the file system keeps the two code points as they are.
        var folder = Files.createDirectory(tempDir.resolve("グ"));
        var track = folder.resolve("tagged.mp3");
        Files.copy(Path.of("src/test/resources/id3/tagged.mp3"), track);

        assertDoesNotThrow(() -> new Media(MediaMusicPlayer.mediaUri(track)));
    }

    @Test
    @DisplayName("存在しないファイルを再生すると、例外を投げずに失敗を通知し、何も読み込んでいない状態になる")
    void playing_a_missing_file_reports_the_failure_and_loads_nothing() {
        var player = new MediaMusicPlayer();
        var reason = new AtomicReference<@Nullable String>();
        player.setOnError(reason::set);

        player.play(Path.of("target/media-test/missing.mp3"));

        assertNotNull(reason.get());
        assertEquals(Optional.empty(), player.playingPath());
    }

    // On the CI runner, which is Windows Server, the test JVM exits with code 1
    // once a MediaPlayer is created. Windows Server has no Media Foundation by
    // default, which is the likely cause. GitHub Actions sets CI=true.
    @Test
    @DisabledIfEnvironmentVariable(
        named = "CI", matches = "true",
        disabledReason = "A MediaPlayer ends the test JVM on the Windows Server runner")
    @DisplayName("開いたあとに再生に失敗すると、失敗を通知し、何も読み込んでいない状態になる")
    void a_failure_after_opening_the_file_is_reported_and_nothing_stays_loaded(FxRobot robot)
        throws Exception {
        var broken = writeZeroFilledMp3(createFolderInTarget("broken"));
        var player = new MediaMusicPlayer();
        var reported = new CompletableFuture<String>();
        // A MediaPlayer reports most failures after it has opened the file,
        // on the JavaFX application thread.
        robot.interact(() -> {
            player.setOnError(reported::complete);
            player.play(broken);
        });

        var reason = reported.get(10, TimeUnit.SECONDS);

        assertTrue(reason.contains("ERROR_MEDIA_INVALID"), reason);
        var loaded = new CompletableFuture<Optional<Path>>();
        robot.interact(() -> loaded.complete(player.playingPath()));
        assertEquals(Optional.empty(), loaded.get());
    }

    // Not @TempDir. Media keeps the file open, and Windows then refuses to
    // delete the folder. A folder in target/ is deleted by mvn clean.
    private static Path createFolderInTarget(String prefix) throws IOException {
        return Files.createTempDirectory(
            Files.createDirectories(Path.of("target/media-test")), prefix
        );
    }

    // The extension says mp3, but the content is not audio. Media accepts the
    // file, and the player fails after it has opened the file.
    private static Path writeZeroFilledMp3(Path folder) throws IOException {
        return Files.write(folder.resolve("broken.mp3"), new byte[4096]);
    }
}
