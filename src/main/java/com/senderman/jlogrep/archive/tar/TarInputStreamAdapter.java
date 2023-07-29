package com.senderman.jlogrep.archive.tar;

import com.senderman.jlogrep.archive.ArchiveEntry;
import com.senderman.jlogrep.archive.ArchiveInputStream;
import org.kamranzafar.jtar.TarInputStream;

import java.io.IOException;

public class TarInputStreamAdapter extends ArchiveInputStream<TarInputStream> {

    public TarInputStreamAdapter(TarInputStream in) {
        super(in);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        var entry = in.getNextEntry();
        if (entry == null)
            return null;
        return new TarEntryAdapter(entry);
    }
}
