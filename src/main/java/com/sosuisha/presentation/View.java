package com.sosuisha.presentation;

import javafx.scene.Scene;

/**
 * A view that provides a scene and a window title.
 */
public interface View {
    /**
     * Returns the scene of this view.
     *
     * @return scene of this view
     */
    Scene getScene();

    /**
     * Returns the window title of this view.
     *
     * @return window title
     */
    String getTitle();
}
