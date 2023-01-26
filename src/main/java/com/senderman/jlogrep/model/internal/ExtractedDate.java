package com.senderman.jlogrep.model.internal;

import io.micronaut.core.annotation.Nullable;

import java.util.Date;

public class ExtractedDate {
    @Nullable
    private final Date date;
    @Nullable
    private final String dateAsString;

    public ExtractedDate(@Nullable Date date, @Nullable String dateAsString) {
        this.date = date;
        this.dateAsString = dateAsString;
    }

    @Nullable
    public Date getDate() {
        return date;
    }

    @Nullable
    public String getDateAsString() {
        return dateAsString;
    }
}
