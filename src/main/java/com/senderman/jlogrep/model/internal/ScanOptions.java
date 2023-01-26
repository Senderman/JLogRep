package com.senderman.jlogrep.model.internal;

import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.LogDateFormat;
import com.senderman.jlogrep.model.rules.RuleFilter;
import com.senderman.jlogrep.model.rules.YamlRule;
import com.senderman.jlogrep.util.RuleConverter;
import io.micronaut.core.annotation.Nullable;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ScanOptions {

    private List<FileRule> fileRules;
    private LogDateFormat dateFormat;
    @Nullable
    private Date problemDate;
    private int interval;
    private int year;
    @Nullable
    private Integer show;
    private Set<String> tags;
    private EnumSet<RuleFilter> filters;

    public ScanOptions(
            List<FileRule> fileRules,
            LogDateFormat dateFormat,
            @Nullable Date problemDate,
            int interval,
            int year,
            @Nullable Integer show,
            Set<String> tags,
            EnumSet<RuleFilter> filters
    ) {
        this.fileRules = fileRules;
        this.dateFormat = dateFormat;
        this.problemDate = problemDate;
        this.interval = interval;
        this.year = year;
        this.show = show;
        this.tags = tags;
        this.filters = filters;
    }

    public List<FileRule> getFileRules() {
        return fileRules;
    }

    public void setYamlRules(List<YamlRule> yamlRules) {
        setFileRules(RuleConverter.toFileRules(yamlRules));
    }

    public void setFileRules(List<FileRule> fileRules) {
        this.fileRules = fileRules;
    }

    public LogDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(LogDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Nullable
    public Date getProblemDate() {
        return problemDate;
    }

    public void setProblemDate(@Nullable Date problemDate) {
        this.problemDate = problemDate;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Nullable
    public Integer getShow() {
        return show;
    }

    public void setShow(@Nullable Integer show) {
        this.show = show;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags;
    }

    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    public void setFilters(EnumSet<RuleFilter> filters) {
        this.filters = filters;
    }
}
