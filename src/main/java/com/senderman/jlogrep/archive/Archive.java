package com.senderman.jlogrep.archive;

import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.util.Stack;

public abstract class Archive {

    private final Stack<Archive> stack;
    private final ArchiveInputStream archiveInputStream;

    public Archive(ArchiveInputStream inputStream) {
        this.stack = new Stack<>();
        this.archiveInputStream = inputStream;
        stack.push(this);
    }

    @Nullable
    public LogSource getNextEntry() {
        // if there's nothing to take from this node
        if (stack.isEmpty())
            return null;
        var archive = stack.peek();
        var in = archive.getArchiveInputStream();
        ArchiveEntry entry;
        try {
            entry = in.getNextEntry();
        } catch (Exception e) { // impossible to unpack current archive
            stack.pop();
            return getNextEntry();
        }
        // if no entries left in the current archive
        if (entry == null) {
            stack.pop();
            return getNextEntry();
        }
        if (entry.isDirectory())
            return getNextEntry();
        var name = entry.getName();
        var clazz = ArchiveDetector.detectArchiveType(name);
        // if the current entry is not an archive
        if (clazz == null) {
            return new LogSource(in, name);
        }
        var nextIn = ArchiveDetector.createArchive(in, clazz);
        stack.push(nextIn);
        return getNextEntry();
    }

    protected ArchiveInputStream getArchiveInputStream() {
        return archiveInputStream;
    }
}
