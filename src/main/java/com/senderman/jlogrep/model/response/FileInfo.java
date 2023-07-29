package com.senderman.jlogrep.model.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
@Schema(description = "Info about file")
public class FileInfo {

    private final String filename;
    private final long size;

    public FileInfo(String filename, long size) {
        this.filename = filename;
        this.size = size;
    }

    @Schema(description = "Name of the file")
    public String getFilename() {
        return filename;
    }

    @Schema(description = "Size of the file in bytes. -1 if unknown")
    public long getSize() {
        return size;
    }
}
