package com.senderman.jlogrep.scanner;

import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.response.Problem;

import java.util.Collection;

public interface LogScanner {

    Collection<Problem> scan(LogsContainer logsContainer, ScanOptions options);

}
