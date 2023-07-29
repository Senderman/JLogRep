package com.senderman.jlogrep.container;

import com.senderman.jlogrep.archive.Archive;
import com.senderman.jlogrep.archive.ArchiveEntry;
import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

import java.util.Stack;

public class ArchiveLogsContainer implements LogsContainer {

    private final Stack<Archive> stack;

    public ArchiveLogsContainer(Archive archive) {
        this.stack = new Stack<>();
        stack.push(archive);
    }

    @Nullable
    public LogSource getNextFileEntry() {
        while (!stack.isEmpty()) {
            var archive = stack.peek();
            var in = archive.getArchiveInputStream();
            ArchiveEntry entry;
            try {
                entry = in.getNextEntry();
            } catch (Exception e) { // impossible to unpack current archive
                stack.pop();
                continue;
            }
            // if no entries left in the current archive
            if (entry == null) {
                stack.pop();
                continue;
            }
            if (entry.isDirectory())
                continue;

            var path = archive.getPath() + "/" + entry.getName();
            var archiveCreator = Archive.getArchiveCreator(path);
            // if the current entry is not an archive, process it as text
            if (archiveCreator == null) {
                return new LogSource(in, path, entry.getSize());
            }
            var nextIn = archiveCreator.apply(path, in);
            stack.push(nextIn);
        }
        // no entries left at all
        return null;
    }
}
