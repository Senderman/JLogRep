package com.senderman.jlogrep.model.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senderman.jlogrep.model.rules.RuleFilter;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Problem {

    private final String name; // name of the problem
    private final transient int show;
    private final boolean shownAlways;
    private final Set<String> tags;
    private final EnumSet<RuleFilter> filters;
    private final transient Predicate<LogString> filter;
    private final TreeSet<LogString> examples; // list of example strings that matched
    private int count; // count of regex matched (independent from examples.size())

    public Problem(String name, int show, boolean shownAlways, Set<String> tags, EnumSet<RuleFilter> filters) {
        this.name = name;
        this.show = show;
        this.shownAlways = shownAlways;
        this.examples = new TreeSet<>();
        this.count = 0;
        this.tags = tags;
        this.filters = filters;
        this.filter = filters.stream()
                .map(Supplier::get)
                .reduce(x -> true, Predicate::and); // join all filters with "AND"
    }

    @JsonGetter
    public String getName() {
        return name;
    }

    @JsonGetter
    public boolean isShownAlways() {
        return shownAlways;
    }

    @JsonGetter
    public int getCount() {
        return count;
    }

    public void incCount() {
        this.count++;
    }

    @JsonGetter
    public Set<String> getTags() {
        return tags;
    }

    // group log lines by file name, mapping grouped values to FileResult
    @JsonGetter
    public Collection<FileResult> getExamples() {
        Map<String, List<LogString>> map = examples
                .stream()
                .skip(Math.max(examples.size() - show, 0))
                .collect(Collectors.groupingBy(LogString::getFile));

        Collection<FileResult> fileResults = new TreeSet<>();
        map.forEach((k, v) -> fileResults.add(new FileResult(k, v)));
        return fileResults;
    }

    @JsonIgnore
    public Problem addProblemsFrom(Problem problem) {
        problem.examples.forEach(this::addNewExample);
        return this;
    }

    /**
     * increases total counter, and adds problem to the list of examples if it has passed all the filters
     *
     * @param logString logString to try to add
     */
    @JsonIgnore
    public void addNewExample(LogString logString) {
        incCount();
        if (filter.test(logString))
            examples.add(logString);
    }

    @JsonGetter
    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    public static class FileResult implements Comparable<FileResult> {
        private final String file;
        private final List<LogString> contents;

        public FileResult(String file, List<LogString> contents) {
            this.file = file;
            this.contents = contents;
        }

        public String getFile() {
            return file;
        }

        @JsonGetter
        public List<LogString> getContents() {
            return contents;
        }

        // compare by first log string date
        @Override
        public int compareTo(FileResult fileResult) {
            return this.contents.get(0).compareTo(fileResult.contents.get(0));
        }
    }
}
