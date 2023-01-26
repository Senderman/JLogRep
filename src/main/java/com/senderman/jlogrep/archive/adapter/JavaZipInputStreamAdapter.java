package com.senderman.jlogrep.archive.adapter;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import java.io.IOException;
import java.util.zip.ZipInputStream;

// this is made because Apache Commons Compress' ZipArchiveInputStream fails to read some zip archives,
// and java.util.zip.ZipInputStream doesn't
public class JavaZipInputStreamAdapter extends ArchiveInputStream {

    private final ZipInputStream in;

    public JavaZipInputStreamAdapter(ZipInputStream in) {
        this.in = in;
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        var entry = in.getNextEntry();
        if (entry == null)
            return null;
        return new JavaZipEntryAdapter(entry);
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }
}
