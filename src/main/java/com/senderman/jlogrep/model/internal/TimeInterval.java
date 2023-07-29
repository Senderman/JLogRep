package com.senderman.jlogrep.model.internal;

import io.micronaut.core.annotation.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

public class TimeInterval implements Predicate<Instant> {
    private final Instant startDate;
    private final Instant endDate;
    private final Predicate<Instant> tester;

    public TimeInterval(@Nullable Instant middleDate, int intervalMinutes) {
        if (middleDate == null) {
            // if middleDate == null, set tester to always return true to ignore date
            tester = d -> true;
            startDate = null;
            endDate = null;
        } else {
            this.startDate = middleDate.minus(intervalMinutes, ChronoUnit.MINUTES);
            this.endDate = middleDate.plus(intervalMinutes, ChronoUnit.MINUTES);
            tester = date -> date.equals(startDate) || date.equals(endDate) || (date.isAfter(startDate) && date.isBefore(endDate));
        }
    }

    // test if the given date is in this time interval
    // will always return true if middleDate is null (see constructor)
    @Override
    public boolean test(Instant date) {
        return tester.test(date);
    }
}