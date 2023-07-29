package com.senderman.jlogrep.scanner;

import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.internal.TimeInterval;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rule.GrepRule;
import com.senderman.jlogrep.model.rule.LogDateFormat;
import com.senderman.jlogrep.model.rule.RuleType;
import com.senderman.jlogrep.scanner.rule.RuleScanner;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class LogScanner {

    private final Map<RuleType, RuleScanner> ruleScanners;

    public LogScanner(List<RuleScanner> ruleScanners) {
        this.ruleScanners = ruleScanners
                .stream()
                .collect(Collectors.toMap(RuleScanner::type, Function.identity()));
    }

    @Timed(value = "scan.logs")
    public List<Problem> scan(LogsContainer logsContainer, ScanOptions options) {

        final var timeInterval = options.getTimeInterval();
        final var tagRules = filterRulesByTags(options.getRules(), options.getTags());

        return Stream.generate(logsContainer::getNextFileEntry)
                .takeWhile(Objects::nonNull)
                .flatMap(s -> {
                    var name = s.getPath().replaceAll(".*/", "");
                    var rules = filterRulesByFileName(tagRules, name);
                    if (rules.size() == 0) return Stream.empty();
                    var dateFormatRule = options.getDateFormat().getFormatFor(name);
                    var lines = s.getLines(dateFormatRule, options.getYear());
                    return rules.stream()
                            .map(r -> mapToProblemSupplier(name, lines, r, timeInterval, options, dateFormatRule))
                            .map(CompletableFuture::supplyAsync);
                })
                .toList() // force push all futures
                .stream()
                .map(CompletableFuture::join)
                .filter(p -> p.getCount() != 0)
                .collect(Collectors.toMap(Problem::getName, Function.identity(), Problem::addProblemsFrom))
                .values()
                .stream()
                .sorted(Comparator.comparing(Problem::isShownAlways).reversed().thenComparing(Problem::getName))
                .toList();
    }

    private Supplier<Problem> mapToProblemSupplier(
            String sourceName,
            List<String> lines,
            GrepRule rule,
            TimeInterval timeInterval,
            ScanOptions options,
            LogDateFormat.DateFormatRule dateFormatRule
    ) {
        return () -> ruleScanners.get(rule.type()).scan(
                sourceName,
                lines,
                rule,
                timeInterval,
                options.getFilters(),
                dateFormatRule
        );
    }

    private Map<String, List<GrepRule>> filterRulesByTags(List<GrepRule> rules, Collection<String> tags) {
        return rules
                .parallelStream()
                .filter(r -> r.showAlways() || tags.contains(r.tag()))
                .collect(Collectors.groupingBy(GrepRule::filename));
    }

    private List<GrepRule> filterRulesByFileName(Map<String, List<GrepRule>> rules, String fileName) {
        return rules
                .entrySet()
                .parallelStream()
                .filter(e -> e.getKey().equals("*") || fileName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
    }

}