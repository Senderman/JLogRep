package com.senderman.jlogrep.model.request;

import com.senderman.jlogrep.model.rule.RuleFilter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.Format;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Schema
public abstract class AbsAnalysisRequest {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.ENGLISH);
    private static final ZoneOffset zoneOffset = ZoneOffset.UTC;

    @Nullable
    private final Instant date;
    private final int year;
    private final int interval;
    private final EnumSet<RuleFilter> filters;

    public AbsAnalysisRequest(
            @Format(DATE_FORMAT) @Nullable String date,
            @Nullable Integer year,
            @Nullable Integer interval,
            @Nullable String filters
    ) {

        this.date = date == null ? null : LocalDateTime.parse(date, df).toInstant(zoneOffset);
        this.year = Objects.requireNonNullElseGet(year, () -> LocalDate.now(ZoneId.of("UTC+0")).getYear());
        this.interval = Objects.requireNonNullElse(interval, 10);

        if (filters == null)
            this.filters = EnumSet.noneOf(RuleFilter.class);
        else
            this.filters = EnumSet.copyOf(Stream.of(filters.split(","))
                    .map(RuleFilter::valueOf)
                    .collect(Collectors.toSet()));
    }

    public @Nullable Instant getDate() {
        return date;
    }

    public int getYear() {
        return year;
    }

    public int getInterval() {
        return interval;
    }

    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

}
