package com.senderman.jlogrep.util;

import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.LogDateFormat;
import com.senderman.jlogrep.model.rules.RuleFilter;
import jakarta.inject.Singleton;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class DefaultScanOptionsSupplier implements Supplier<ScanOptions> {

    private final List<FileRule> fileRules;
    private final LogDateFormat dateFormat;

    public DefaultScanOptionsSupplier(List<FileRule> fileRules, LogDateFormat dateFormat) {
        this.fileRules = fileRules;
        this.dateFormat = dateFormat;
    }

    @Override
    public ScanOptions get() {
        return new ScanOptions(
                fileRules,
                dateFormat,
                null,
                10,
                Calendar.getInstance().get(Calendar.YEAR),
                null,
                new HashSet<>(),
                EnumSet.noneOf(RuleFilter.class)
        );
    }
}
