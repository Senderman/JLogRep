package com.senderman.jlogrep.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senderman.jlogrep.model.rule.RuleFilter;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Serdeable
@Schema(description = "Problem, found in logs")
public class Problem {

    private final String name; // name of the problem
    private final int show; // how much examples to show (for frontend)
    private final boolean shownAlways;
    private final String tag;
    private final EnumSet<RuleFilter> filters;
    private final transient Predicate<LogString> filter;
    private final transient Deque<LogString> examples; // list of example strings that matched
    private int count; // count of regex matched (independent from examples.size()), because examples could be filtered

    public Problem(String name, int show, boolean shownAlways, String tag, EnumSet<RuleFilter> filters) {
        this.name = name;
        this.show = show;
        this.shownAlways = shownAlways;
        this.examples = new ArrayDeque<>();
        this.count = 0;
        this.tag = tag;
        this.filters = filters;
        this.filter = filters.stream()
                .map(Supplier::get)
                .reduce(x -> true, Predicate::and); // join all filters with "AND"
    }

    @Schema(description = "Name of the problem. Defined in rules")
    public String getName() {
        return name;
    }

    @Schema(description = "How many examples to show by default. For frontend")
    public int getShow() {
        return show;
    }

    @Schema(description = "True if this problem shows up regardless of tags selected")
    public boolean isShownAlways() {
        return shownAlways;
    }

    @Schema(description = "How many lines matches this rule")
    public int getCount() {
        return count;
    }

    public void incCount() {
        this.count++;
    }

    @Schema(description = "Tag, associated with this problem. Defined in rules")
    public String getTag() {
        return tag;
    }

    // group log lines by file name, mapping grouped values to FileResult
    @Schema(description = "Examples, grouped by file")
    public List<FileResult> getExamples() {
        return examples
                .stream()
                .sorted()
                .collect(Collectors.groupingBy(LogString::getFile))
                .entrySet()
                .parallelStream()
                .map(e -> new FileResult(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(r -> r.contents.get(0)))
                .toList();
    }

    @Schema(description = "Size of 'examples' array")
    public int getExamplesSize() {
        return examples.size();
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

    @Schema(description = "Filters applied to this rule")
    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    @Serdeable
    @Schema(description = "Matched lines in single file")
    public static class FileResult {
        private final String file;
        private final List<LogString> contents;

        public FileResult(String file, List<LogString> contents) {
            this.file = file;
            this.contents = contents;
        }

        @Schema(description = "File name")
        public String getFile() {
            return file;
        }

        @Schema(description = "Matched lines")
        public List<LogString> getContents() {
            return contents;
        }
    }
}
