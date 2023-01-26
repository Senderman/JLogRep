package com.senderman.jlogrep.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class TarGzArchive extends Archive {

    public TarGzArchive(InputStream inputStream) throws IOException {
        super(new TarArchiveInputStream(new GZIPInputStream(inputStream)));
    }

}
