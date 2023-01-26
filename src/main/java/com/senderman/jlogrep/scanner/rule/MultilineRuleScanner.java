package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleType;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class MultilineRuleScanner extends RuleScanner {

    @Override
    public RuleType type() {
        return RuleType.MULTILINE;
    }

    @Override
    protected boolean preservesDateInOutput() {
        return true;
    }

    @Override
    protected String onNextLine(String line, List<String> lines, int lineNumber, FileRule.GrepRule rule) {
        if (rule.getRegexes().size() != 2) {
            throw new InvalidConfigFormatException("rules.yml");
        }
        var startRegex = rule.getRegexes().get(0);
        var endRegex = rule.getRegexes().get(1);

        if (!startRegex.matcher(line).matches())
            return null;

        return line + "<br>" + scanUntilEnd(lines, lineNumber + 1, endRegex);
    }

    private String scanUntilEnd(List<String> source, int start, Pattern endRegex) {
        return source
                .stream()
                .skip(start)
                .takeWhile(l -> !endRegex.matcher(l).matches())
                .collect(Collectors.joining("<br>"));

    }
}
