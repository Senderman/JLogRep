package com.senderman.jlogrep.util;

import io.micronaut.http.multipart.CompletedFileUpload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Uploads {

    public static InputStream copy(CompletedFileUpload upload) throws IOException {
        return new ByteArrayInputStream(upload.getInputStream().readAllBytes());
    }

}
