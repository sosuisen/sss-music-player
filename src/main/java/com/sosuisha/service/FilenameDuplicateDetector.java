package com.sosuisha.service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sosuisha.domain.model.DuplicatedItems;
import com.sosuisha.domain.service.DuplicateDetector;

/**
 * Detects duplicated files by their file name.
 */
public class FilenameDuplicateDetector implements DuplicateDetector {
    private final List<Path> files;

    /**
     * Creates the detector.
     *
     * @param files paths of the files to examine
     * @throws NullPointerException if files is null
     */
    public FilenameDuplicateDetector(List<Path> files) {
        this.files = Objects.requireNonNull(files, "files must not be null");
    }

    /**
     * Returns groups of files whose file names are the same. A group contains
     * two or more files. Groups and their files keep the encounter order.
     *
     * @return groups of duplicated files
     */
    @Override
    public List<DuplicatedItems> detect() {
        var groups = files.stream()
            .collect(
                Collectors.groupingBy(
                    path -> path.getFileName().toString(),
                    LinkedHashMap::new,
                    Collectors.toList()
                )
            );
        return groups.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2)
            .map(entry -> new DuplicatedItems(entry.getKey(), entry.getValue()))
            .toList();
    }
}
