package com.sosuisha.presentation.screens.librarymanager.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FavoriteStarTest {
    @Test
    @DisplayName("星の記号は、登録済みなら★、未登録なら☆である")
    void the_star_is_filled_for_a_favorite_and_hollow_otherwise() {
        assertEquals("★", FavoriteStar.glyph(true));
        assertEquals("☆", FavoriteStar.glyph(false));
    }
}
