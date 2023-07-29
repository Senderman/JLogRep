package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.exception.InvalidConfigFormatException;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.RuleType;
import com.senderman.jlogrep.util.ComparableArrayDeque;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class MultilineRuleScanner extends RuleScanner {

    @Override
    public RuleType type() {
        return RuleType.MULTILINE;
    }

    @Override
    protected ComparableArrayDeque<String> onNextLine(List<String> lines, int lineNumber, GrepRule rule) {
        if (rule.regexes().size() != 2) {
            throw new InvalidConfigFormatException("rules.yml", "for MULTILINE type the amount of rules has to be 2");
        }
        var startRegex = rule.regexes().get(0);
        var endRegex = rule.regexes().get(1);

        var line = lines.get(lineNumber);
        if (!startRegex.matcher(line).find())
            return emptyDeque;

        return Stream.concat(Stream.of(line), scanUntilEnd(lines, lineNumber + 1, endRegex))
                .collect(Collectors.toCollection(ComparableArrayDeque::new));
    }

    private Stream<String> scanUntilEnd(List<String> source, int start, Pattern endRegex) {
        return source
                .stream()
                .skip(start)
                .takeWhile(l -> !endRegex.matcher(l).find());

    }
}
