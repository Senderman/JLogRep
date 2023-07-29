package com.senderman.jlogrep.model.rule;

import java.util.List;

public class ConfigRule {

    private String name;
    private RuleType type;
    private int show;
    private List<Pattern> patterns;
    private String tag;
    private boolean showAlways;
    private List<String> filters;

    public ConfigRule() {
        this.type = RuleType.SIMPLE;
        this.show = 5;
        this.showAlways = false;
        this.filters = List.of();
    }

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

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isShowAlways() {
        return showAlways;
    }

    public void setShowAlways(boolean showAlways) {
        this.showAlways = showAlways;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public static class Pattern {

        private String file;
        private List<String> regexes;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public List<String> getRegexes() {
            return regexes;
        }

        public void setRegexes(List<String> regexes) {
            this.regexes = regexes;
        }
    }

}
