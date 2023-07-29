package com.senderman.jlogrep.util;

import com.senderman.jlogrep.model.rule.*;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConfigConverter {

    public static List<GrepRule> toGrepRules(List<ConfigRule> configRules) {
        return configRules
                .parallelStream()
                .flatMap(r -> r.getPatterns()
                        .parallelStream()
                        .map(p -> new GrepRule(
                                p.getFile(),
                                r.getName(),
                                r.getType(),
                                r.getShow(),
                                r.isShowAlways(),
                                p.getRegexes()
                                        .parallelStream()
                                        .map(Pattern::compile)
                                        .toList(),
                                r.getTag(),
                                r.getFilters().isEmpty()
                                        ? EnumSet.noneOf(RuleFilter.class)
                                        : EnumSet.copyOf(r.getFilters()
                                        .stream()
                                        .map(RuleFilter::valueOf)
                                        .toList())
                        )))
                .toList();
    }

    public static LogDateFormat toLogDateFormat(ConfigDateFormat configDateFormat) {
        var rules = configDateFormat.getRules()
                .parallelStream()
                .map(ConfigConverter::toDateFormatRule)
                .toList();

        return new LogDateFormat(toDateFormatRule(configDateFormat.getDefaultRule()), rules);
    }

    private static LogDateFormat.DateFormatRule toDateFormatRule(ConfigDateFormat.ConfigDateFormatRule dateFormatRule) {
        return new LogDateFormat.DateFormatRule(
                dateFormatRule.getFile(),
                DateTimeFormatter.ofPattern(dateFormatRule.getFormat(), Locale.ENGLISH),
                dateFormatRule.isPossibleMissingYear(),
                dateFormatRule.getYearSuffix(),
                Pattern.compile("(" + dateFormatRule.getRegex() + ")")
        );
    }

}
