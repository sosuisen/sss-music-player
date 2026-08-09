package com.sosuisha.presentation.screens.duplicatelist;

import com.sosuisha.presentation.View;

import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.scene.Scene;

/**
 * View for the duplicate file list screen.
 */
public class DuplicateListView implements View {
    private static final String TITLE = "Duplicate Files";

    private final Scene scene;

    /**
     * Creates the view.
     */
    public DuplicateListView() {
        this.scene = buildSceneGraph();
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    private Scene buildSceneGraph() {
        return SceneBuilder
            .withRoot(VBoxBuilder.create().build())
            .build();
    }
}
