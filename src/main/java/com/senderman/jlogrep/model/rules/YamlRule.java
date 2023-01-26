package com.senderman.jlogrep.model.rules;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class YamlRule {

    @JsonProperty(required = true)
    private String name;
    @JsonProperty
    private RuleType type = RuleType.SIMPLE;
    @JsonProperty
    private int show = 5; // how many examples to show in the output. Default is 5
    @JsonProperty(required = true)
    private List<Pattern> patterns;
    @JsonProperty(required = true)
    private Set<String> tags;
    @JsonProperty
    private boolean showAlways = false;
    @JsonProperty
    private EnumSet<RuleFilter> filters = EnumSet.noneOf(RuleFilter.class);

    public String getName() {
        return name;
    }

    public RuleType getType() {
        return type;
    }

    public int getShow() {
        return show;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public Set<String> getTags() {
        return tags;
    }

    public boolean isShowAlways() {
        return showAlways;
    }

    public EnumSet<RuleFilter> getFilters() {
        return filters;
    }

    public static class Pattern {
        @JsonProperty(required = true)
        private String file;
        @JsonProperty(required = true)
        private List<String> regexes;

        public String getFile() {
            return file;
        }

        public List<String> getRegexes() {
            return regexes;
        }
    }

}
