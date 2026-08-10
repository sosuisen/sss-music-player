package com.sosuisha.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.model.MusicFile;
import com.sosuisha.domain.service.DuplicateDetector;

/**
 * Detects duplicated files by their file name and size.
 */
public class FilenameAndSizeDuplicateDetector implements DuplicateDetector {
    private record GroupKey(String filename, long size) {
    }

    private final List<MusicFile> files;

    /**
     * Creates the detector.
     *
     * @param files files to examine
     * @throws NullPointerException if files is null
     */
    public FilenameAndSizeDuplicateDetector(List<MusicFile> files) {
        this.files = Objects.requireNonNull(files, "files must not be null");
    }

    /**
     * Returns groups of files whose file names and sizes are both the same. A
     * group contains two or more files. Groups and their files keep the
     * encounter order.
     *
     * @return groups of duplicated files
     */
    @Override
    public List<DuplicatedItems> detect() {
        var groups = files.stream()
            .collect(
                Collectors.groupingBy(
                    file -> new GroupKey(file.path().getFileName().toString(), file.size()),
                    LinkedHashMap::new,
                    Collectors.toList()
                )
            );
        return groups.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2)
            .map(entry -> new DuplicatedItems(entry.getKey().filename(), entry.getValue()))
            .toList();
    }
}
