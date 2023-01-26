package com.senderman.jlogrep.model.rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// same as YamlRule, but it's optimized because you can group it by filename
public class FileRule {

    @JsonProperty(required = true)
    private final String file;

    @JsonProperty(required = true)
    private final List<GrepRule> rules;

    public String getFile() {
        return file;
    }

    public List<GrepRule> getRules() {
        return rules;
    }

    public boolean matches(String fileName) {
        return file.equals("*") || fileName.contains(this.file);
    }

    public FileRule(String file, List<GrepRule> rules) {
        this.file = file;
        this.rules = rules;
    }

    public static class GrepRule {

        private String name;
        private RuleType type;
        private int show;
        private boolean showAlways = false;
        private List<Pattern> regexes;
        private Set<String> tags;
        private EnumSet<RuleFilter> filters;
        private Pattern joinedPattern; // regexes but joined with |

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public RuleType getType() {
            return type;
        }

        public void setType(RuleType type) {
            this.type = type;
        }

        public int getShow() {
            return show;
        }

        public void setShow(int show) {
            this.show = show;
        }

        public boolean isShowAlways() {
            return showAlways;
        }

        public void setShowAlways(boolean showAlways) {
            this.showAlways = showAlways;
        }

        public List<Pattern> getRegexes() {
            return regexes;
        }

        public void setRegexes(List<String> regexes) {
            this.regexes = regexes.parallelStream().map(r -> Pattern.compile(".*" + r + ".*")).collect(Collectors.toUnmodifiableList());
            this.joinedPattern = Pattern.compile(
                    this.regexes.stream().map(Pattern::pattern).collect(Collectors.joining("|"))
            );
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public EnumSet<RuleFilter> getFilters() {
            return filters;
        }

        public void setFilters(EnumSet<RuleFilter> filters) {
            this.filters = filters;
        }

        public boolean matches(Collection<String> tags) {
            return isShowAlways() || getTags().stream().anyMatch(tags::contains);
        }

        public Pattern getJoinedPattern() {
            return joinedPattern;
        }
    }

}
