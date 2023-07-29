package com.senderman.jlogrep.archive;

import java.io.IOException;
import java.io.InputStream;

public abstract class ArchiveInputStream<T extends InputStream> extends InputStream {

    protected final T in;

    public ArchiveInputStream(T in) {
        this.in = in;
    }

    public abstract ArchiveEntry getNextEntry() throws IOException;

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
