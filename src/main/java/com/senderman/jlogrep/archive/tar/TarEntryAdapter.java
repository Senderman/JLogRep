package com.senderman.jlogrep.archive.tar;

import com.senderman.jlogrep.archive.ArchiveEntry;
import org.kamranzafar.jtar.TarEntry;

public class TarEntryAdapter implements ArchiveEntry {

    private final TarEntry entry;

    public TarEntryAdapter(TarEntry entry) {
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
