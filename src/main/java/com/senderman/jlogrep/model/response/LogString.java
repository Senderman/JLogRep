package com.senderman.jlogrep.model.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.core.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogString implements Comparable<LogString> {

    private final static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

    private final transient String file;
    private final String line;
    @Nullable
    private final Date date;

    // Date will be saved as new Date(0) if null is given
    public LogString(String file, String line, @Nullable Date date) {
        this.file = file;
        this.line = line;
        this.date = date;
    }

    @JsonIgnore
    public String getFile() {
        return file;
    }

    public String getLine() {
        return line;
    }

    @Nullable
    @JsonIgnore
    public Date getRawDate() {
        return date;
    }

    @JsonGetter
    public String getDate() {
        return date != null ? df.format(date) : "";
    }

    @Override
    public int compareTo(LogString logString) {
        Date o1Date = this.getRawDate();
        Date o2Date = logString.getRawDate();
        if (o1Date == null || o2Date == null)
            return 1;

        if (o1Date.before(o2Date))
            return -1;
        else
            return 1;

        // we don't compare o1Date.after because if they are equal, and we return 0, TreeSet won't add it
    }
}
