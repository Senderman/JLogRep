package com.senderman.jlogrep.archive.zip;

import com.senderman.jlogrep.archive.ArchiveEntry;
import com.senderman.jlogrep.archive.ArchiveInputStream;

import java.io.IOException;
import java.util.zip.ZipInputStream;

public class ZipInputStreamAdapter extends ArchiveInputStream<ZipInputStream> {

    public ZipInputStreamAdapter(ZipInputStream in) {
        super(in);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        var entry = in.getNextEntry();
        if (entry == null)
            return null;
        return new ZipEntryAdapter(entry);
    }

}
