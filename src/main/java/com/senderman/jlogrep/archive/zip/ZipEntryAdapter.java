package com.senderman.jlogrep.archive.zip;

import com.senderman.jlogrep.archive.ArchiveEntry;

import java.util.zip.ZipEntry;

public class ZipEntryAdapter implements ArchiveEntry {

    private final ZipEntry entry;

    public ZipEntryAdapter(ZipEntry entry) {
        this.entry = entry;
    }

    @Override
    public String getName() {
        return entry.getName();
    }

    @Override
    public long getSize() {
        return entry.getSize();
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

}
