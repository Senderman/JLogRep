package com.senderman.jlogrep.model.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.senderman.jlogrep.model.rules.RuleFilter;
import io.micronaut.core.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbsAnalysisRequest {

    private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    @Nullable
    Date date;

    int year = Calendar.getInstance().get(Calendar.YEAR);

    private int interval = 10;

    @Nullable
    private Integer show;

    @Nullable
    private EnumSet<RuleFilter> filters = EnumSet.noneOf(RuleFilter.class);

    @Nullable
    @JsonGetter
    public Date getDate() {
        return date;
    }

    @JsonSetter
    public void setDate(@Nullable String date) throws ParseException {
        if (date == null)
            return;
        this.date = df.parse(date);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Nullable
    public Integer getShow() {
        return show;
    }

    public void setShow(@Nullable Integer show) {
        this.show = show;
    }

    @Nullable
    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    public void setFilters(@Nullable String filters) {
        if (filters == null)
            return;

        this.filters = EnumSet.copyOf(Stream.of(filters.split(","))
                .map(RuleFilter::valueOf)
                .collect(Collectors.toSet()));
    }

}
