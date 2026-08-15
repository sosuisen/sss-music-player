package com.sosuisha.presentation.screens.librarymanager;

/**
 * Sort key of the album list.
 */
public enum SortKey {
    /** Sorts the albums by album name. */
    ALBUM("Album"),
    /** Sorts the albums by album artist name. */
    ARTIST("Artist");

    private final String label;

    SortKey(String label) {
        this.label = label;
    }

    /**
     * Returns the label of the sort key shown in the UI.
     *
     * @return label of the sort key
     */
    @Override
    public String toString() {
        return label;
    }
}
