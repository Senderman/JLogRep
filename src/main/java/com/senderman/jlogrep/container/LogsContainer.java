package com.senderman.jlogrep.container;

import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

public interface LogsContainer {

    @Nullable
    LogSource getNextEntry();
}
