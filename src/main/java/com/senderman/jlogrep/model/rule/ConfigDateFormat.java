package com.senderman.jlogrep.model.rule;

import java.util.List;

public class ConfigDateFormat {

    private ConfigDateFormatRule defaultRule;
    private List<ConfigDateFormatRule> rules;

    public ConfigDateFormatRule getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(ConfigDateFormatRule defaultRule) {
        this.defaultRule = defaultRule;
    }

    public List<ConfigDateFormatRule> getRules() {
        return rules;
    }

    public void setRules(List<ConfigDateFormatRule> rules) {
        this.rules = rules;
    }

    public static class ConfigDateFormatRule {

        private String file;
        private String format;
        private boolean possibleMissingYear;
        private String yearSuffix;
        private String regex;

        public ConfigDateFormatRule() {
            this.possibleMissingYear = false;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public boolean isPossibleMissingYear() {
            return possibleMissingYear;
        }

        public void setPossibleMissingYear(boolean possibleMissingYear) {
            this.possibleMissingYear = possibleMissingYear;
        }

        public String getYearSuffix() {
            return yearSuffix;
        }

        public void setYearSuffix(String yearSuffix) {
            this.yearSuffix = yearSuffix;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
