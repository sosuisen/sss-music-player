package com.sosuisha.domain.model;

/**
 * Color theme of the application.
 */
public enum Theme {
    /** Primer light theme. */
    PRIMER_LIGHT("Primer Light"),
    /** Primer dark theme. */
    PRIMER_DARK("Primer Dark"),
    /** Nord light theme. */
    NORD_LIGHT("Nord Light"),
    /** Nord dark theme. */
    NORD_DARK("Nord Dark"),
    /** Cupertino light theme. */
    CUPERTINO_LIGHT("Cupertino Light"),
    /** Cupertino dark theme. */
    CUPERTINO_DARK("Cupertino Dark"),
    /** Dracula theme. */
    DRACULA("Dracula");

    private final String label;

    Theme(String label) {
        this.label = label;
    }

    /**
     * Returns the label of the theme shown in the UI.
     *
     * @return label of the theme
     */
    @Override
    public String toString() {
        return label;
    }
}
