package com.senderman.jlogrep.container;

import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

import java.io.InputStream;

public class PlainTextLogsContainer implements LogsContainer {

    private final LogSource logSource;
    private boolean isRequested;

    public PlainTextLogsContainer(InputStream in, String fileName, long size) {
        this.logSource = new LogSource(in, fileName, size);
        this.isRequested = false;
    }

    @Override
    @Nullable
    public LogSource getNextFileEntry() {
        if (isRequested)
            return null;

        isRequested = true;
        return logSource;
    }

}
