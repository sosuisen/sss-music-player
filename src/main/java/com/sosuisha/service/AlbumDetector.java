package com.sosuisha.service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sosuisha.domain.model.Album;
import com.sosuisha.domain.model.MusicFile;

/**
 * Recognizes albums by the album name and the album artist of the track
 * metadata. A file without an album name falls back to recognition by its
 * parent folder.
 */
public class AlbumDetector {
    private static final Pattern FOUR_DIGIT_YEAR = Pattern.compile("\\d{4}");

    private sealed interface GroupKey permits MetadataKey, FolderKey {
    }

    private record MetadataKey(String name, String artist) implements GroupKey {
    }

    private record FolderKey(Path folder) implements GroupKey {
    }

    private final List<MusicFile> files;

    /**
     * Creates the detector.
     *
     * @param files files to examine
     * @throws NullPointerException if files is null
     */
    public AlbumDetector(List<MusicFile> files) {
        this.files = Objects.requireNonNull(files, "files must not be null");
    }

    /**
     * Returns the albums recognized in the files. Files whose album name and
     * album artist are both the same form one album. Files whose album name is
     * empty form one album per parent folder instead, whose name is the folder
     * name and whose artist is empty. The year of an album is the first
     * non-empty year of its files, reduced to its four-digit year when the tag
     * holds a full date, or an empty string when every year is empty. Albums
     * and their files keep the encounter order.
     *
     * @return recognized albums
     */
    public List<Album> detect() {
        var groups = files.stream()
            .collect(
                Collectors.groupingBy(
                    AlbumDetector::keyOf, LinkedHashMap::new, Collectors.toList()
                )
            );
        return groups.entrySet().stream()
            .map(entry -> toAlbum(entry.getKey(), entry.getValue()))
            .toList();
    }

    private static GroupKey keyOf(MusicFile file) {
        if (file.tag().album().isEmpty()) { return new FolderKey(file.path().getParent()); }
        return new MetadataKey(file.tag().album(), file.tag().albumArtist());
    }

    private static Album toAlbum(GroupKey key, List<MusicFile> albumFiles) {
        var year = firstNonEmptyYear(albumFiles);
        return switch (key) {
            case MetadataKey(String name, String artist) -> new Album(
                name, artist, year, albumFiles
            );
            case FolderKey(Path folder) -> new Album(
                folder == null ? "" : folder.getFileName().toString(), "", year, albumFiles
            );
        };
    }

    private static String firstNonEmptyYear(List<MusicFile> albumFiles) {
        return albumFiles.stream()
            .map(file -> file.tag().year())
            .filter(year -> !year.isEmpty())
            .findFirst()
            .map(AlbumDetector::toFourDigitYear)
            .orElse("");
    }

    private static String toFourDigitYear(String year) {
        var matcher = FOUR_DIGIT_YEAR.matcher(year);
        return matcher.find() ? matcher.group() : year;
    }
}
