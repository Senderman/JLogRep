package com.senderman.jlogrep.scanner;

import com.senderman.jlogrep.container.LogsContainer;
import com.senderman.jlogrep.model.internal.ScanOptions;
import com.senderman.jlogrep.model.internal.TimeInterval;
import com.senderman.jlogrep.model.response.Problem;
import com.senderman.jlogrep.model.rules.FileRule;
import com.senderman.jlogrep.model.rules.RuleType;
import com.senderman.jlogrep.scanner.rule.RuleScanner;
import com.senderman.jlogrep.util.LogSource;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class LogScannerImpl implements LogScanner {

    private final Map<RuleType, RuleScanner> ruleScanners;

    public LogScannerImpl(List<RuleScanner> ruleScanners) {
        this.ruleScanners = ruleScanners
                .stream()
                .collect(Collectors.toMap(RuleScanner::type, Function.identity()));
    }

    @Override
    public Collection<Problem> scan(LogsContainer logsContainer, ScanOptions options) {

        var timeInterval = new TimeInterval(options.getProblemDate(), options.getInterval());

        final List<FileRule> fileRules = filterRulesByTags(options.getFileRules(), options.getTags());

        return Stream.generate(logsContainer::getNextEntry)
                .takeWhile(Objects::nonNull)
                .flatMap(s -> {
                    List<FileRule.GrepRule> grepRules = filterRulesByFileName(fileRules, s.getName());
                    if (grepRules.size() == 0) return Stream.empty();
                    s.prepare(options.getDateFormat().getFormatFor(s.getName()), options.getYear());
                    return grepRules.stream()
                            .map(r -> mapToProblemSupplier(s, options, r, timeInterval))
                            .map(CompletableFuture::supplyAsync);
                })
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .filter(p -> p.getCount() != 0)
                .collect(Collectors.groupingBy(Problem::getName, Collectors.reducing(Problem::addProblemsFrom)))
                .values()
                .stream()
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(Problem::getCount).reversed())
                .collect(Collectors.toList());
    }

    private Supplier<Problem> mapToProblemSupplier(LogSource source, ScanOptions options, FileRule.GrepRule rule, TimeInterval interval) {
        return () -> ruleScanners.get(rule.getType()).scan(source, options, rule, interval);
    }

    private List<FileRule> filterRulesByTags(Collection<FileRule> rules, Collection<String> tags) {
        return rules.parallelStream()
                .map(r -> new FileRule(r.getFile(),
                        r.getRules().parallelStream()
                                .filter(g -> g.matches(tags))
                                .collect(Collectors.toUnmodifiableList()))
                )
                .filter(r -> !r.getRules().isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    private List<FileRule.GrepRule> filterRulesByFileName(Collection<FileRule> rules, String fileName) {
        return rules
                .parallelStream()
                .filter(r -> r.matches(fileName))
                .flatMap(r -> r.getRules().stream())
                .collect(Collectors.toUnmodifiableList());
    }

}