package com.senderman.jlogrep.scanner;

import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.model.response.FileInfo;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Singleton
public class FileListScanner {

    @Timed("scan.files")
    public List<FileInfo> scan(LogsContainer logsContainer) {
        return Stream.generate(logsContainer::getNextFileEntry)
                .takeWhile(Objects::nonNull)
                .map(s -> new FileInfo(s.getPath().replaceAll(".*/", ""), s.getSize())) // remove path from file name
                .sorted(Comparator.comparing(FileInfo::getFilename))
                .toList();
    }

}
