package com.senderman.jlogrep.model.rule;

import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

// same as ConfigRule, but it's optimized because you can group it by filename
public record GrepRule(
        String filename,
        String name,
        RuleType type,
        int show,
        boolean showAlways,
        List<Pattern> regexes,
        String tag,
        EnumSet<RuleFilter> filters
) {

}