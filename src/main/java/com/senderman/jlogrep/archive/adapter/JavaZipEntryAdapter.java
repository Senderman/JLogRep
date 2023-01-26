package com.senderman.jlogrep.archive.adapter;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.util.Date;
import java.util.zip.ZipEntry;

// this is made because Apache Commons Compress' ZipArchiveInputStream fails to read some zip archives,
// and java.util.zip.ZipInputStream doesn't
public class JavaZipEntryAdapter implements ArchiveEntry {

    private final ZipEntry entry;

    public JavaZipEntryAdapter(ZipEntry entry) {
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

    @Override
    public Date getLastModifiedDate() {
        return new Date(entry.getLastModifiedTime().toMillis());
    }
}
