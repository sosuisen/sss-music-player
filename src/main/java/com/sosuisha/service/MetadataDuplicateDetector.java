package com.sosuisha.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.DuplicateDetector;

/**
 * Detects duplicated files by the title, the artist, and the album of their
 * track metadata.
 */
public class MetadataDuplicateDetector implements DuplicateDetector {
    private record GroupKey(String title, String artist, String album) {
    }

    private final List<MusicFile> files;

    /**
     * Creates the detector.
     *
     * @param files files to examine
     * @throws NullPointerException if files is null
     */
    public MetadataDuplicateDetector(List<MusicFile> files) {
        this.files = Objects.requireNonNull(files, "files must not be null");
    }

    /**
     * Returns groups of files whose titles, artists, and albums are all the
     * same. A group contains two or more files. A file whose title or artist
     * is empty is excluded; an empty album still takes part in the match.
     * Groups keep the encounter order. The files in a group are
     * ordered by size, largest first, so that the duplicate remover keeps the
     * largest file; files of the same size keep the encounter order.
     *
     * @return groups of duplicated files
     */
    @Override
    public List<DuplicatedItems> detect() {
        var groups = files.stream()
            .filter(file -> !file.tag().title().isEmpty() && !file.tag().artist().isEmpty())
            .collect(
                Collectors.groupingBy(
                    file -> new GroupKey(
                        file.tag().title(), file.tag().artist(), file.tag().album()
                    ),
                    LinkedHashMap::new,
                    Collectors.toList()
                )
            );
        return groups.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2)
            .map(
                entry -> new DuplicatedItems(
                    entry.getKey().title() + " - " + entry.getKey().artist(),
                    orderBySizeDescending(entry.getValue())
                )
            )
            .toList();
    }

    private static List<MusicFile> orderBySizeDescending(List<MusicFile> files) {
        // Stream.sorted is stable, so files of the same size keep the
        // encounter order.
        return files.stream()
            .sorted(Comparator.comparingLong(MusicFile::size).reversed())
            .toList();
    }
}
