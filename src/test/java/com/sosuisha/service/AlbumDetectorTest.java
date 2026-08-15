package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

class AlbumDetectorTest {
    @Test
    @DisplayName("アルバム名とアルバムアーティストが同じファイルが、1つのアルバムにまとまる")
    void files_with_the_same_album_and_album_artist_form_one_album() {
        var albumA1 = new MusicFile(Path.of("a/one.mp3"), 100, tag("Album A", "Artist X"));
        var albumA2 = new MusicFile(Path.of("b/two.mp3"), 200, tag("Album A", "Artist X"));
        var albumB = new MusicFile(Path.of("c/three.mp3"), 300, tag("Album B", "Artist X"));
        var detector = new AlbumDetector(List.of(albumA1, albumA2, albumB));

        assertEquals(
            List.of(
                new Album("Album A", "Artist X", List.of(albumA1, albumA2)),
                new Album("Album B", "Artist X", List.of(albumB))
            ),
            detector.detect()
        );
    }

    @Test
    @DisplayName("アルバム名が空のファイルは、同じ親フォルダごとに1つのアルバムになる")
    void files_with_an_empty_album_form_one_album_per_parent_folder() {
        var hitsA = new MusicFile(Path.of("music/Best Hits/one.mp3"), 100, tag("", ""));
        var hitsB = new MusicFile(Path.of("music/Best Hits/two.mp3"), 200, tag("", ""));
        var other = new MusicFile(Path.of("music/Other/three.mp3"), 300, tag("", ""));
        var detector = new AlbumDetector(List.of(hitsA, hitsB, other));

        assertEquals(
            List.of(
                new Album("Best Hits", "", List.of(hitsA, hitsB)),
                new Album("Other", "", List.of(other))
            ),
            detector.detect()
        );
    }

    @Test
    @DisplayName("親フォルダが無いファイルは、空の名前のアルバムになる")
    void a_file_without_a_parent_folder_forms_an_album_with_an_empty_name() {
        var rootFile = new MusicFile(Path.of("one.mp3"), 100, tag("", ""));
        var detector = new AlbumDetector(List.of(rootFile));

        assertEquals(List.of(new Album("", "", List.of(rootFile))), detector.detect());
    }

    @Test
    @DisplayName("アルバムの年は、ファイルのタグの最初の空でない年になる")
    void the_year_of_an_album_is_the_first_non_empty_year_of_its_files() {
        var noYear = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("", "", "Album A", "Artist X", "", "")
        );
        var year2001 = new MusicFile(
            Path.of("a/two.mp3"), 200,
            new TrackMetadata("", "", "Album A", "Artist X", "", "2001")
        );
        var year2002 = new MusicFile(
            Path.of("a/three.mp3"), 300,
            new TrackMetadata("", "", "Album A", "Artist X", "", "2002")
        );
        var detector = new AlbumDetector(List.of(noYear, year2001, year2002));

        assertEquals(
            List.of(
                new Album("Album A", "Artist X", "2001", List.of(noYear, year2001, year2002))
            ),
            detector.detect()
        );
    }

    private static TrackMetadata tag(String album, String albumArtist) {
        return new TrackMetadata("", "", album, albumArtist, "", "");
    }
}
