package com.senderman.jlogrep.util;

import com.senderman.jlogrep.exception.WrongDateRegexException;
import com.senderman.jlogrep.model.rule.LogDateFormat;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single log source (e.g. file, stream) with log lines
 */
public class LogSource {

    private static final Charset charset = StandardCharsets.UTF_8;
    private static final byte[] UTF8_BOM = {-17, -69, -65};

    private final String path;
    private final InputStream inputStream;
    private final long size;
    private boolean isUsed;

    public LogSource(InputStream inputStream, String path, long size) {
        this.inputStream = inputStream;
        this.path = path;
        this.size = size;
        this.isUsed = false;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public List<String> getLines(LogDateFormat.DateFormatRule dateFormatRule, int year) {
        if (isUsed)
            throw new IllegalStateException("getLines method was called before for file " + path);
        try {
            var buff = new BufferedInputStream(inputStream);
            final int CHAR_SIZE = 2;
            // skip BOM if exists
            byte[] bomBuffer = new byte[UTF8_BOM.length];
            buff.mark(bomBuffer.length);
            buff.read(bomBuffer);
            boolean bomExists = Arrays.equals(bomBuffer, UTF8_BOM);
            buff.reset();
            if (bomExists)
                buff.readNBytes(bomBuffer.length);

            // calculate year prefix
            byte[] firstLineBuffer = new byte[CHAR_SIZE * 50];
            buff.mark(firstLineBuffer.length);
            buff.read(firstLineBuffer);
            boolean yearIsMissing = checkIfYearMissing(firstLineBuffer, dateFormatRule);
            buff.reset();

            var linesStream = new BufferedReader(new InputStreamReader(buff, charset)).lines();
            this.isUsed = true;
            if (!yearIsMissing) {
                return linesStream.toList();
            } else {
                return linesStream
                        .map(l -> year + dateFormatRule.yearSuffix() + l)
                        .toList();
            }

        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Error while processing " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns full path to this log source
     *
     * @return full path to this log source
     */
    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    private boolean checkIfYearMissing(byte[] data, LogDateFormat.DateFormatRule rule) {
        if (!rule.possibleMissingYear())
            return false;

        try {
            rule.extractDate(new String(data, charset));
            return false;
        } catch (DateTimeParseException | WrongDateRegexException e) {
            return true;
        }
    }
}
