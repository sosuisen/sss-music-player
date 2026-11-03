package com.sosuisha.presentation.screens.librarymanager.components;

import java.util.Objects;

import javafx.scene.Cursor;
import javafx.scene.control.Label;

/**
 * Component that shows the star of a list row. Clicking the star toggles the
 * favorite of the row.
 */
public class FavoriteStar {
    static final String STYLE_CLASS = "favorite-star";

    private FavoriteStar() {}

    /**
     * Returns the star glyph for the given state: a filled star for a
     * favorite, and a hollow star otherwise.
     *
     * @param favorite whether the row is a favorite
     * @return star glyph
     */
    public static String glyph(boolean favorite) {
        return favorite ? "★" : "☆";
    }

    /**
     * Creates the star of a list row.
     *
     * @param favorite whether the row is a favorite
     * @param onClick action that toggles the favorite of the row
     * @return label that shows the star
     * @throws NullPointerException if onClick is null
     */
    public static Label create(boolean favorite, Runnable onClick) {
        Objects.requireNonNull(onClick, "onClick must not be null");
        var star = new Label(glyph(favorite));
        star.getStyleClass().add(STYLE_CLASS);
        star.setCursor(Cursor.HAND);
        star.setOnMouseClicked(_ -> onClick.run());
        return star;
    }
}
