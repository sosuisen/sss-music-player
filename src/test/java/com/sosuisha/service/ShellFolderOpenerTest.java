package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sosuisha.domain.exception.FolderOpenException;

class ShellFolderOpenerTest {
    @TempDir
    Path folder;

    @Test
    @DisplayName("ファイルマネージャーのコマンドが起動できない場合、openはFolderOpenExceptionを投げる")
    void open_throws_FolderOpenException_when_the_file_manager_command_cannot_be_launched() {
        var opener = new ShellFolderOpener("no-such-file-manager-command");

        assertThrows(FolderOpenException.class, () -> opener.open(folder));
    }
}
