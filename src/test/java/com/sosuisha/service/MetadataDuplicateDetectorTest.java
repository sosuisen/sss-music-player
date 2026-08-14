package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.model.TrackMetadata;

class MetadataDuplicateDetectorTest {
    @Test
    @DisplayName("曲名とアーティストが両方同じファイルだけが、重複グループになる")
    void only_files_with_the_same_title_and_the_same_artist_form_a_duplicated_group() {
        var sameA = new MusicFile(Path.of("a/one.mp3"), 100, tag("Song", "Artist"));
        var sameB = new MusicFile(Path.of("b/two.mp3"), 100, tag("Song", "Artist"));
        var differentArtist = new MusicFile(Path.of("c/three.mp3"), 300, tag("Song", "Other"));
        var detector = new MetadataDuplicateDetector(List.of(sameA, sameB, differentArtist));

        var duplicatedItems = detector.detect();

        assertEquals(
            List.of(new DuplicatedItems("Song - Artist", List.of(sameA, sameB))),
            duplicatedItems
        );
    }

    @Test
    @DisplayName("グループ内のファイルはサイズの大きい順に並び、同サイズは発見順を保つ")
    void files_in_a_group_are_ordered_by_size_descending_and_ties_keep_encounter_order() {
        // The duplicate remover keeps the first file of a group, so this order
        // makes it keep the largest file.
        var smallest = new MusicFile(Path.of("a/one.mp3"), 100, tag("Song", "Artist"));
        var largestFirst = new MusicFile(Path.of("b/two.mp3"), 300, tag("Song", "Artist"));
        var largestSecond = new MusicFile(Path.of("c/three.mp3"), 300, tag("Song", "Artist"));
        var detector = new MetadataDuplicateDetector(
            List.of(smallest, largestFirst, largestSecond)
        );

        assertEquals(
            List.of(
                new DuplicatedItems(
                    "Song - Artist", List.of(largestFirst, largestSecond, smallest)
                )
            ),
            detector.detect()
        );
    }

    @Test
    @DisplayName("曲名またはアーティストが空のファイルは、判定から除外される")
    void files_with_an_empty_title_or_artist_are_excluded_from_detection() {
        var noTitleA = new MusicFile(Path.of("a/one.mp3"), 100, tag("", "Artist"));
        var noTitleB = new MusicFile(Path.of("b/two.mp3"), 200, tag("", "Artist"));
        var noArtistA = new MusicFile(Path.of("c/three.mp3"), 300, tag("Song", ""));
        var noArtistB = new MusicFile(Path.of("d/four.mp3"), 400, tag("Song", ""));
        var detector = new MetadataDuplicateDetector(
            List.of(noTitleA, noTitleB, noArtistA, noArtistB)
        );

        assertEquals(List.of(), detector.detect());
    }

    @Test
    @DisplayName("曲名とアーティストが同じでも、アルバムが異なるファイルは同じグループにならない")
    void files_with_a_different_album_do_not_form_a_group_even_with_the_same_title_and_artist() {
        var albumA = new MusicFile(
            Path.of("a/one.mp3"), 100,
            new TrackMetadata("Song", "Artist", "Album A", "", "", "")
        );
        var albumB = new MusicFile(
            Path.of("b/two.mp3"), 100,
            new TrackMetadata("Song", "Artist", "Album B", "", "", "")
        );
        var detector = new MetadataDuplicateDetector(List.of(albumA, albumB));

        assertEquals(List.of(), detector.detect());
    }

    private static TrackMetadata tag(String title, String artist) {
        return new TrackMetadata(title, artist, "", "", "", "");
    }
}
