package com.sosuisha.domain.service;

import java.util.List;

import com.sosuisha.domain.model.DuplicatedItems;

/**
 * Detects groups of duplicated files.
 */
@FunctionalInterface
public interface DuplicateDetector {
    /**
     * Returns the groups of duplicated files.
     *
     * @return groups of duplicated files
     */
    List<DuplicatedItems> detect();
}
