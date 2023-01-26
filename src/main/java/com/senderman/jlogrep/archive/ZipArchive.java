package com.senderman.jlogrep.archive;

import com.senderman.jlogrep.archive.adapter.JavaZipInputStreamAdapter;

import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZipArchive extends Archive {

    public ZipArchive(InputStream inputStream) {
        super(new JavaZipInputStreamAdapter(new ZipInputStream(inputStream)));
    }

}
