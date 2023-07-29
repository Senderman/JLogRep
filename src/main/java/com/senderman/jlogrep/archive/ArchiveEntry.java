package com.senderman.jlogrep.archive;

public interface ArchiveEntry {

    String getName();

    long getSize();

    boolean isDirectory();

}
