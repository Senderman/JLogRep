package com.senderman.jlogrep.container;

import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

/**
 * Represents a container with logs, which provides a {@link LogSource} object for each log entry
 */
public interface LogsContainer {

    /**
     * get next log file
     *
     * @return the next log file (wrapped in LogSource object) or null if no more left
     */
    @Nullable
    LogSource getNextFileEntry();
}
