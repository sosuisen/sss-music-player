package com.sosuisha.presentation.screens.librarymanager.components;

import java.util.Objects;

import com.sosuisha.presentation.screens.librarymanager.LibraryManagerViewModel;

import io.github.sosuisen.jfxbuilder.controls.MenuBarBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuBuilder;
import io.github.sosuisen.jfxbuilder.controls.MenuItemBuilder;
import javafx.scene.control.MenuBar;

/**
 * Component that shows the menu bar of the library manager screen.
 */
public class LibraryMenuBar {
    private LibraryMenuBar() {}

    /**
     * Returns the root node of the component.
     *
     * @param viewModel view model of the library manager screen
     * @return menu bar of the library manager screen
     * @throws NullPointerException if viewModel is null
     */
    public static MenuBar getRoot(LibraryManagerViewModel viewModel) {
        Objects.requireNonNull(viewModel, "viewModel must not be null");
        return MenuBarBuilder
            .withMenus(
                MenuBuilder
                    .withItems(
                        MenuItemBuilder.create()
                            .text("Rescan")
                            .onAction(_ -> viewModel.rescan())
                            .build(),
                        MenuItemBuilder.create()
                            .text("Remove duplicate files...")
                            .onAction(_ -> viewModel.openDuplicateListWindow())
                            .build(),
                        MenuItemBuilder.create()
                            .text("Settings...")
                            .onAction(_ -> viewModel.openSettingsWindow())
                            .build()
                    )
                    .text("File")
                    .build()
            )
            .build();
    }
}
