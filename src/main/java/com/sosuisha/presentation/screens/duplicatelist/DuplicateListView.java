package com.sosuisha.presentation.screens.duplicatelist;

import java.util.Objects;

import com.sosuisha.presentation.View;
import com.sosuisha.presentation.screens.duplicatelist.components.CandidateList;
import com.sosuisha.presentation.screens.duplicatelist.components.DetailedPanel;

import io.github.sosuisen.jfxbuilder.controls.ButtonBuilder;
import io.github.sosuisen.jfxbuilder.controls.LabelBuilder;
import io.github.sosuisen.jfxbuilder.controls.SplitPaneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.HBoxBuilder;
import io.github.sosuisen.jfxbuilder.graphics.SceneBuilder;
import io.github.sosuisen.jfxbuilder.graphics.VBoxBuilder;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the duplicate file list screen.
 */
public class DuplicateListView implements View {
    private static final String TITLE = "Duplicate Files";

    private final DuplicateListViewModel viewModel;
    private final Scene scene;

    /**
     * Creates the view.
     *
     * @param viewModel view model of the duplicate file list screen
     * @throws NullPointerException if viewModel is null
     */
    public DuplicateListView(DuplicateListViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel must not be null");
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
        // Why the VBox: the theme (AtlantaFX) paints the window background on
        // the root node of the scene through its ".root" style. However, the
        // theme also makes a SplitPane transparent so that the parent shows
        // through it. If the SplitPane itself were the root, nothing would be
        // painted behind it and the window would show the plain white default
        // fill of the Scene instead of the theme color. The VBox is a root
        // that takes the ".root" background, and the SplitPane shows it
        // through.
        return SceneBuilder
            .withRoot(
                VBoxBuilder
                    .withChildren(
                        SplitPaneBuilder
                            .withItems(
                                buildCandidatePane(),
                                DetailedPanel.getRoot(viewModel)
                            )
                            .vGrowInVBox(Priority.ALWAYS)
                            .build()
                    )
                    .build()
            )
            .build();
    }

    private VBox buildCandidatePane() {
        var candidateList = CandidateList.getRoot(viewModel);
        VBox.setVgrow(candidateList, Priority.ALWAYS);
        return VBoxBuilder
            .withChildren(
                HBoxBuilder
                    .withChildren(
                        ButtonBuilder.create()
                            .text("Find by Filename")
                            .id("findByFilename")
                            .onAction(_ -> viewModel.detectByFilename())
                            .build(),
                        ButtonBuilder.create()
                            .text("Find by Filename and Size")
                            .id("findByFilenameAndSize")
                            .onAction(_ -> viewModel.detectByFilenameAndSize())
                            .build(),
                        ButtonBuilder.create()
                            .text("Find by Metadata")
                            .id("findByMetadata")
                            .onAction(_ -> viewModel.detectByMetadata())
                            .build()
                    )
                    .spacing(10)
                    .build(),
                candidateList,
                LabelBuilder.create()
                    .id("errorLabel")
                    .style("-fx-text-fill: red;")
                    .textPropertyApply(
                        prop -> prop.bind(viewModel.errorMessageProperty())
                    )
                    .build(),
                HBoxBuilder
                    .withChildren(
                        ButtonBuilder.create()
                            .text("Toggle all")
                            .id("toggleAll")
                            .onAction(_ -> viewModel.toggleAllChecks())
                            .build(),
                        ButtonBuilder.create()
                            .text("Remove checked duplicates")
                            .id("removeDuplicates")
                            .onAction(_ -> viewModel.removeCheckedDuplicates())
                            .disablePropertyApply(
                                prop -> prop.bind(viewModel.anyCheckedProperty().not())
                            )
                            .build()
                    )
                    .spacing(10)
                    .alignment(Pos.CENTER_RIGHT)
                    .build()
            )
            .build();
    }
}
