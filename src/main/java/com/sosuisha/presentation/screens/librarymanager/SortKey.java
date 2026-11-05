package com.sosuisha.presentation.screens.librarymanager;

/**
 * Sort key of the album list. {@link #FAVORITES} replaces the albums with the
 * favorites in the order they were added.
 */
public enum SortKey {
    /** Sorts the albums by album name. */
    ALBUM("Album"),
    /** Sorts the albums by album artist name. */
    ARTIST("Artist"),
    /** Shows all tracks sorted by title instead of the albums. */
    TRACK("Track"),
    /** Shows the favorite albums and tracks in the order they were added. */
    FAVORITES("Favorites");

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
