package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.RuleType;
import com.senderman.jlogrep.util.ComparableArrayDeque;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class SimpleRuleScanner extends RuleScanner {

    @Override
    public RuleType type() {
        return RuleType.SIMPLE;
    }

    @Override
    protected ComparableArrayDeque<String> onNextLine(List<String> lines, int lineNumber, GrepRule rule) {
        var line = lines.get(lineNumber);
        for (var regex : rule.regexes()) {
            var matcher = regex.matcher(line);
            if (!matcher.find())
                continue;

            // if there's no capturing groups in the current regex
            if (matcher.groupCount() == 0) {
                var result = new ComparableArrayDeque<String>();
                result.add(line);
                return result;
            }

            matcher.reset();
            return matcher.results()
                    .flatMap(this::extractGroups)
                    .collect(Collectors.toCollection(ComparableArrayDeque::new));
        }
        return emptyDeque;
    }

    private Stream<String> extractGroups(MatchResult matchResult) {
        return Stream.generate(new Supplier<String>() {

            int current = 1;

            @Override
            public String get() {
                return matchResult.group(current++);
            }
        }).limit(matchResult.groupCount());
    }
}
