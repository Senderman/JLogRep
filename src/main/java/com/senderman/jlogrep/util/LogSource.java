package com.senderman.jlogrep.util;

import com.senderman.jlogrep.exception.ZeroGroupsFoundException;
import com.senderman.jlogrep.model.internal.ExtractedDate;
import com.senderman.jlogrep.model.rules.LogDateFormat;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogSource {

    private final Charset charset = StandardCharsets.UTF_8;
    private final String name;
    private final InputStream inputStream;
    private List<String> source;
    private boolean isPrepared;
    private SourceSettings settings;

    public LogSource(InputStream inputStream, String name) {
        this.inputStream = inputStream;
        this.name = name.replaceAll(".*/", "");
        this.isPrepared = false;
    }

    // call this method before using!
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void prepare(LogDateFormat.DateFormatRule dateFormatRule, int year) {
        if (isPrepared)
            return;
        try {
            var buff = new BufferedInputStream(inputStream);
            final int CHAR_SIZE = 2;
            // skip BOM if exists
            byte[] bomBuffer = new byte[CHAR_SIZE];
            buff.mark(bomBuffer.length);
            buff.read(bomBuffer);
            boolean bomExists = checkIfBomExists(bomBuffer);
            buff.reset();
            if (bomExists)
                buff.readNBytes(CHAR_SIZE);

            // calculate year prefix
            byte[] firstLineBuffer = new byte[CHAR_SIZE * 50];
            buff.mark(firstLineBuffer.length);
            buff.read(firstLineBuffer);
            boolean yearIsMissing = checkIfYearMissing(firstLineBuffer, dateFormatRule);
            buff.reset();
            this.settings = new SourceSettings(dateFormatRule);

            var linesStream = new BufferedReader(new InputStreamReader(buff, charset)).lines();
            if (!yearIsMissing) {
                this.source = linesStream.collect(Collectors.toUnmodifiableList());
            } else {
                this.source = linesStream
                        .map(l -> year + dateFormatRule.getYearSuffix() + l)
                        .collect(Collectors.toUnmodifiableList());
            }

            this.isPrepared = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> lines() {
        if (!isPrepared)
            throw new IllegalStateException("LogSource is unprepared! Call the prepare() method before using newInputStream()!");
        return source;
    }

    public ExtractedDate extractDateFromLogString(String line) throws ParseException, ZeroGroupsFoundException {
        if (!isPrepared)
            throw new IllegalStateException("LogSource is unprepared! Call the prepare() method before using newInputStream()!");
        return extractDateFromLogString(line, settings.dateFormatRule.getPattern(), settings.dateFormat.get());
    }

    private ExtractedDate extractDateFromLogString(String line, Pattern regex, DateFormat dateFormat) throws ParseException, ZeroGroupsFoundException {
        var matcher = regex.matcher(line);
        if (!matcher.matches())
            throw new ZeroGroupsFoundException(line, regex.pattern());
        String dateString = matcher.group(1);
        return new ExtractedDate(dateFormat.parse(dateString), dateString);
    }

    private boolean checkIfYearMissing(byte[] data, LogDateFormat.DateFormatRule rule) {
        if (!rule.getPossibleMissingYear())
            return false;

        String line = new String(data, charset);
        try {
            var dateFormat = new SimpleDateFormat(rule.getFormat(), Locale.ENGLISH);
            extractDateFromLogString(line, rule.getPattern(), dateFormat);
            return false;
        } catch (ParseException | ZeroGroupsFoundException e) {
            return true;
        }
    }

    private boolean checkIfBomExists(byte[] data) {
        return new String(data, charset).charAt(0) == '\ufeff';
    }

    private static class SourceSettings {
        private final LogDateFormat.DateFormatRule dateFormatRule;
        private final ThreadLocal<DateFormat> dateFormat;


        public SourceSettings(LogDateFormat.DateFormatRule dateFormatRule) {
            this.dateFormatRule = dateFormatRule;
            this.dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(dateFormatRule.getFormat(), Locale.ENGLISH));
        }

    }
}
