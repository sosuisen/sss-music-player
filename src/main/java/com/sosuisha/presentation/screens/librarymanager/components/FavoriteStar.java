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
     * Creates the star of a list row.
     *
     * @param onClick action that toggles the favorite of the row
     * @return label that shows the star
     * @throws NullPointerException if onClick is null
     */
    public static Label create(Runnable onClick) {
        Objects.requireNonNull(onClick, "onClick must not be null");
        var star = new Label("☆");
        star.getStyleClass().add(STYLE_CLASS);
        star.setCursor(Cursor.HAND);
        star.setOnMouseClicked(_ -> onClick.run());
        return star;
    }
}
