package com.senderman.jlogrep.container;

import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

import java.io.InputStream;

public class PlainTextLogsContainer implements LogsContainer {

    private final LogSource inputStream;
    private boolean isRequested;

    public PlainTextLogsContainer(InputStream inputStream, String fileName) {
        this.inputStream = new LogSource(inputStream, fileName);
        this.isRequested = false;
    }

    @Override
    @Nullable
    public LogSource getNextEntry() {
        if (isRequested)
            return null;

        isRequested = true;
        return inputStream;
    }
}
