package com.sosuisha.main;

import java.util.Objects;

import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application of SSS Music Player.
 */
public class App extends Application {
    private static final String TITLE = "SSS Music Player";
    private static final double WIDTH = 640;
    private static final double HEIGHT = 400;

    /**
     * Called when the application is started.
     *
     * @param stage the primary stage for this application
     * @throws NullPointerException if stage is null
     */
    @Override
    public void start(Stage stage) {
        Objects.requireNonNull(stage, "stage must not be null");
        stage.setScene(buildSceneGraph());
        stage.setTitle(TITLE);
        stage.show();
    }

    private Scene buildSceneGraph() {
        return SceneBuilder
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        LabelBuilder.create()
                            .text(TITLE)
                            .style("-fx-font-weight: bold;")
                            .build()
                    )
                    .alignment(Pos.CENTER)
                    .build()
            )
            .width(WIDTH)
            .height(HEIGHT)
            .build();
    }
}
