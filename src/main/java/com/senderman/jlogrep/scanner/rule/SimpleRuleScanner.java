package com.senderman.jlogrep.scanner.rule;

import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleType;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class SimpleRuleScanner extends RuleScanner {

    @Override
    public RuleType type() {
        return RuleType.SIMPLE;
    }

    @Override
    protected boolean preservesDateInOutput() {
        return true;
    }

    @Override
    protected String onNextLine(String line, List<String> lines, int lineNumber, FileRule.GrepRule rule) {
        return rule.getJoinedPattern().matcher(line).matches() ? line : null;
    }
}
