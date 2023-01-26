package com.senderman.jlogrep.model.internal;

import io.micronaut.core.annotation.Nullable;

import java.util.Date;
import java.util.function.Predicate;

public class TimeInterval implements Predicate<Date> {
    private final Date startDate;
    private final Date endDate;
    private final Predicate<Date> tester;

    public TimeInterval(@Nullable Date middleDate, int intervalMinutes) {
        if (middleDate == null) {
            // if middleDate == null, set tester to always return true to ignore date
            tester = d -> true;
            startDate = null;
            endDate = null;
        } else {
            long minutesToMillis = intervalMinutes * 60000L;
            // prevent startDate from being less than 0
            this.startDate = new Date(Math.max(middleDate.getTime() - minutesToMillis, 0));
            this.endDate = new Date(middleDate.getTime() + minutesToMillis);
            tester = date -> date.equals(startDate) || date.equals(endDate) || (date.after(startDate) && date.before(endDate));
        }
    }

    // test if the given date is in this time interval
    // will always return true if middleDate is null (see constructor)
    @Override
    public boolean test(Date date) {
        return tester.test(date);
    }
}