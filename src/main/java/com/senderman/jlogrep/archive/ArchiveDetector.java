package com.senderman.jlogrep.archive;

import io.micronaut.core.annotation.Nullable;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ArchiveDetector {

    private static final Map<String, Class<? extends Archive>> supportedExtensions = Map.of(
            "zip", ZipArchive.class,
            "tar.gz", TarGzArchive.class
    );

    private static final String extensionRegex = ".*(" + String.join("|", supportedExtensions.keySet()) + ")";


    public static Archive createArchive(InputStream inputStream, Class<? extends Archive> clazz) {
        try {
            return clazz
                    .getConstructor(InputStream.class)
                    .newInstance(inputStream);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Class<? extends Archive> detectArchiveType(String fileName) {
        var ext = fileName.replaceAll(extensionRegex, "$1");
        return supportedExtensions.get(ext);
    }

}
