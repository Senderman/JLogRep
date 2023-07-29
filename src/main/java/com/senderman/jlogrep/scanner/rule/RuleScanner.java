package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.exception.WrongDateRegexException;
import com.senderman.jlogrep.model.internal.TimeInterval;
import com.senderman.jlogrep.model.response.LogString;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.LogDateFormat;
import com.senderman.jlogrep.model.rule.RuleFilter;
import com.senderman.jlogrep.model.rule.RuleType;
import com.senderman.jlogrep.util.ComparableArrayDeque;

import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;

public abstract class RuleScanner {

    protected static final ComparableArrayDeque<String> emptyDeque = new ComparableArrayDeque<>(0);

    /**
     * Type of the rule for which this scanner will be used
     *
     * @return Type of the rule for which this scanner will be used
     */
    public abstract RuleType type();

    public Problem scan(
            String sourceName,
            List<String> lines,
            GrepRule rule,
            TimeInterval timeInterval,
            EnumSet<RuleFilter> filters,
            LogDateFormat.DateFormatRule dateFormatRule
    ) {
        // merge filters from the rule self and from scan options
        final EnumSet<RuleFilter> allFilters = EnumSet.copyOf(rule.filters());
        allFilters.addAll(filters);

        final var problem = new Problem(rule.name(), rule.show(), rule.showAlways(), rule.tag(), allFilters);
        for (int i = 0; i < lines.size(); i++) {

            String originalLine = lines.get(i);
            var resultLines = onNextLine(lines, i, rule);
            if (resultLines.isEmpty())
                continue;

            try {
                var extractedDate = dateFormatRule.extractDate(originalLine);
                if (!timeInterval.test(extractedDate))
                    continue;

                // remove date from the first result line
                resultLines.push(dateFormatRule.pattern().matcher(resultLines.pop()).replaceFirst(""));
                problem.addNewExample(new LogString(sourceName, resultLines, extractedDate));

            } catch (DateTimeParseException | WrongDateRegexException e) {
                problem.addNewExample(new LogString(sourceName, resultLines, null));
            }
        }

        return problem;
    }

    /**
     * Process new line from the log. Implementations can return a modified first line if needed
     *
     * @param lines      all lines from the log.
     * @param lineNumber index of the line passed
     * @param rule       rule associated with this file
     * @return ComparableArrayDeque, which consists of lines that would be added to output, or empty deque. Note that date, if exists, will be removed from the first line
     */
    protected abstract ComparableArrayDeque<String> onNextLine(List<String> lines, int lineNumber, GrepRule rule);

}
