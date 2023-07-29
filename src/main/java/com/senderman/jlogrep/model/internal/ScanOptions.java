package com.senderman.jlogrep.model.internal;

import com.senderman.jlogrep.model.rule.ConfigRule;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.LogDateFormat;
import com.senderman.jlogrep.model.rule.RuleFilter;
import com.senderman.jlogrep.util.ConfigConverter;
import io.micronaut.core.annotation.Nullable;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ScanOptions {

    private List<GrepRule> rules;
    private LogDateFormat dateFormat;
    @Nullable
    private Instant problemDate;
    private int interval;
    private int year;
    private Set<String> tags;
    private EnumSet<RuleFilter> filters;

    public ScanOptions(
            List<GrepRule> rules,
            LogDateFormat dateFormat,
            @Nullable Instant problemDate,
            int interval,
            int year,
            Set<String> tags,
            EnumSet<RuleFilter> filters
    ) {
        this.rules = rules;
        this.dateFormat = dateFormat;
        this.problemDate = problemDate;
        this.interval = interval;
        this.year = year;
        this.tags = tags;
        this.filters = filters;
    }

    public List<GrepRule> getRules() {
        return rules;
    }

    public ScanOptions setRules(List<GrepRule> rules) {
        this.rules = rules;
        return this;
    }

    public ScanOptions setConfigRules(List<ConfigRule> configRules) {
        setRules(ConfigConverter.toGrepRules(configRules));
        return this;
    }

    public LogDateFormat getDateFormat() {
        return dateFormat;
    }

    public ScanOptions setDateFormat(LogDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public ScanOptions setProblemDate(@Nullable Instant problemDate) {
        this.problemDate = problemDate;
        return this;
    }

    public ScanOptions setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public TimeInterval getTimeInterval() {
        return new TimeInterval(problemDate, interval);
    }

    public int getYear() {
        return year;
    }

    public ScanOptions setYear(int year) {
        this.year = year;
        return this;
    }

    public ScanOptions setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    public ScanOptions setFilters(EnumSet<RuleFilter> filters) {
        this.filters = filters;
        return this;
    }
}
