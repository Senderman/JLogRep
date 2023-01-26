package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleType;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ExtractingRuleScanner extends RuleScanner {
    @Override
    public RuleType type() {
        return RuleType.EXTRACT;
    }

    @Override
    protected boolean preservesDateInOutput() {
        return false;
    }

    @Override
    protected String onNextLine(String line, List<String> lines, int lineNumber, FileRule.GrepRule rule) {
        List<String> allExtractedFromLine = new ArrayList<>(rule.getRegexes().size());
        for (var regex : rule.getRegexes()) {
            var matcher = regex.matcher(line);
            int groupCount = matcher.groupCount();
            if (!matcher.matches())
                continue;

            String[] extractedText = new String[groupCount];

            for (int i = 1; i <= groupCount; i++) {
                extractedText[i - 1] = matcher.group(i);
            }
            allExtractedFromLine.add(String.join(", ", extractedText));
        }
        return allExtractedFromLine.size() == 0 ? null : String.join("<br>", allExtractedFromLine);
    }
}
