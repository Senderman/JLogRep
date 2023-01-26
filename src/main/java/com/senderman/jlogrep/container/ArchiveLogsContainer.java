package com.senderman.jlogrep.container;

import com.senderman.jlogrep.archive.Archive;
import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

public class ArchiveLogsContainer implements LogsContainer {

    private final Archive archive;

    public ArchiveLogsContainer(Archive archive) {
        this.archive = archive;
    }

    @Nullable
    public LogSource getNextEntry() {
        return archive.getNextEntry();
    }
}
