package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.exception.ZeroGroupsFoundException;
import com.senderman.jlogrep.model.internal.ExtractedDate;
import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.internal.TimeInterval;
import com.senderman.jlogrep.model.response.LogString;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleFilter;
import com.senderman.jlogrep.model.rules.RuleType;
import com.senderman.jlogrep.util.LogSource;
import io.micronaut.core.annotation.Nullable;

import java.text.ParseException;
import java.util.EnumSet;
import java.util.List;

public abstract class RuleScanner {

    /**
     * Type of the rule for which this scanner will be used
     *
     * @return Type of the rule for which this scanner will be used
     */
    public abstract RuleType type();

    /**
     * Should return true if the {@link #onNextLine} method preserves string's date in the returned value.
     * Note that if this returns true, it's expected that string contents before the date wouldn't be modified, too.
     *
     * @return true if the {@link #onNextLine} method preserves string's date in the returned value, else false
     */
    protected abstract boolean preservesDateInOutput();

    public Problem scan(LogSource source, ScanOptions options, FileRule.GrepRule rule, TimeInterval timeInterval
    ) {
        int show = options.getShow() == null ? rule.getShow() : options.getShow();
        EnumSet<RuleFilter> filters = EnumSet.copyOf(rule.getFilters());
        filters.addAll(options.getFilters());

        var problem = new Problem(rule.getName(), show, rule.isShowAlways(), rule.getTags(), filters);
        var lines = source.lines();
        for (int i = 0; i < lines.size(); i++) {

            String originalLine = lines.get(i);
            var resultLine = onNextLine(originalLine, lines, i, rule);
            if (resultLine == null)
                continue;

            var extractedDate = extractDateFromLogString(source, originalLine);
            String lineToSave;

            if (extractedDate.getDate() == null || extractedDate.getDateAsString() == null) {
                lineToSave = resultLine;
            } else {
                if (!timeInterval.test(extractedDate.getDate()))
                    continue;

                // remove date from the result line
                lineToSave = removeDateIfNeeded(resultLine, extractedDate.getDateAsString());
            }

            problem.addNewExample(new LogString(source.getName(), lineToSave, extractedDate.getDate()));
        }


        return problem;
    }

    /**
     * Process new line from the log. Implementations can return a modified line if needed
     *
     * @param line       new line from the log
     * @param lines      lines from the log
     * @param lineNumber number of the line passed
     * @param rule       rule associated with this file
     * @return line that would be added to output, or null. Note that date will be removed from the resulting line
     */
    @Nullable
    protected abstract String onNextLine(String line, List<String> lines, int lineNumber, FileRule.GrepRule rule);

    /**
     * Extract date from log line, using the log source
     *
     * @param source LogSource for the source of the line
     * @param line   the line with date
     * @return date, parsed from the line. If it fails to parse it, all fields in the result object will be null
     */
    protected ExtractedDate extractDateFromLogString(LogSource source, String line) {
        try {
            return source.extractDateFromLogString(line);
        } catch (ParseException | ZeroGroupsFoundException e) {
            return new ExtractedDate(null, null);
        }
    }

    // remove date from line if the scanner didn't do it itself
    private String removeDateIfNeeded(String line, String dateString) {
        return preservesDateInOutput()
                ? line.substring(dateString.length())
                : line;
    }

}
