package com.senderman.jlogrep.util;

import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.YamlRule;

import java.util.*;
import java.util.stream.Collectors;

// converts human-readable rules.yml to more optimized version (FileRule)
public class RuleConverter {

    public static List<FileRule> toFileRules(List<YamlRule> yamlRules) {
        Map<String, List<FileRule.GrepRule>> fileRules = new HashMap<>();
        for (var rule : yamlRules) {
            for (var pattern : rule.getPatterns()) {
                var grepRule = new FileRule.GrepRule();
                grepRule.setName(rule.getName());
                grepRule.setType(rule.getType());
                grepRule.setShow(rule.getShow());
                grepRule.setShowAlways(rule.isShowAlways());
                grepRule.setRegexes(pattern.getRegexes());
                grepRule.setTags(rule.getTags());
                grepRule.setFilters(rule.getFilters());

                fileRules.compute(pattern.getFile(), (k, v) -> {
                    List<FileRule.GrepRule> list = Objects.requireNonNullElseGet(v, ArrayList::new);
                    list.add(grepRule);
                    return list;
                });
            }
        }

        return fileRules
                .entrySet()
                .stream()
                .map(e -> new FileRule(e.getKey(), e.getValue()))
                .collect(Collectors.toUnmodifiableList());
    }

}
