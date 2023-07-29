package com.senderman.jlogrep.model.request;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.Format;

import java.util.Objects;

public class RegexRequest extends AbsAnalysisRequest {

    private final String regex;
    private final String file;

    public RegexRequest(
            @Format(DATE_FORMAT) @Nullable String date,
            @Nullable Integer year,
            @Nullable Integer interval,
            @Nullable String filters,
            @Nullable String regex,
            @Nullable String file
    ) {
        super(date, year, interval, filters);
        this.regex = Objects.requireNonNullElse(regex, ".*");
        this.file = Objects.requireNonNullElse(file, "*");
    }

    public String getRegex() {
        return regex;
    }

    public String getFile() {
        return file;
    }

}
