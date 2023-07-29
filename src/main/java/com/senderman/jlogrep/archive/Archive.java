package com.senderman.jlogrep.archive;

import com.senderman.jlogrep.archive.tar.TarInputStreamAdapter;
import com.senderman.jlogrep.archive.zip.ZipInputStreamAdapter;
import io.micronaut.core.annotation.Nullable;
import org.kamranzafar.jtar.TarInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public abstract class Archive {

    private static final Map<String, BiFunction<String, InputStream, Archive>> supportedExtensions = Map.of(
            "zip",
            (name, in) -> new Archive(name, new ZipInputStreamAdapter(new ZipInputStream(in))) {
            },

            "tar.gz",
            (name, in) -> new Archive(name, new TarInputStreamAdapter(new TarInputStream(uncheckedGzipInputStream(in)))) {
            }
    );

    private static final Pattern extensionRegex = Pattern.compile(".*(" + String.join("|", supportedExtensions.keySet()) + ")");
    private final ArchiveInputStream<?> archiveInputStream;
    private final String path;

    /**
     * Constructs Archive object with given path and archive input stream.
     * It's expected that inputStream contains the correct decompressor for this archive
     *
     * @param path        full path to this archive
     * @param inputStream input stream, wrapped with appropriate decompressor
     */
    private Archive(String path, ArchiveInputStream<?> inputStream) {
        this.path = path;
        this.archiveInputStream = inputStream;
    }

    /**
     * Return Archive creator based on file extension, or null if no suitable creator found
     *
     * @param fileName name of the file
     * @return InputStream -> Archive function, or null if no suitable creator found
     */
    @Nullable
    public static BiFunction<String, InputStream, Archive> getArchiveCreator(String fileName) {
        return supportedExtensions.get(extensionRegex.matcher(fileName).replaceFirst("$1"));
    }

    private static GZIPInputStream uncheckedGzipInputStream(InputStream in) {
        try {
            return new GZIPInputStream(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ArchiveInputStream<?> getArchiveInputStream() {
        return archiveInputStream;
    }

    /**
     * Returns full path to this archive
     *
     * @return full path to this archive
     */
    public String getPath() {
        return path;
    }

}
