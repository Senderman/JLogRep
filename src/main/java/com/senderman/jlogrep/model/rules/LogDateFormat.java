package com.senderman.jlogrep.model.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.micronaut.core.annotation.Nullable;

import java.util.List;
import java.util.regex.Pattern;

public class LogDateFormat {

    @JsonProperty("default")
    private DateFormatRule defaultFormatRule;
    @JsonProperty
    private List<DateFormatRule> rules;

    public DateFormatRule getDefaultFormatRule() {
        return defaultFormatRule;
    }

    public List<DateFormatRule> getRules() {
        return rules;
    }

    public DateFormatRule getFormatFor(String fileName) {
        return rules.parallelStream()
                .filter(r -> fileName.contains(r.file))
                .findAny()
                .orElseGet(this::getDefaultFormatRule);
    }

    public static class DateFormatRule {
        @JsonProperty(required = true)
        private String file;
        @JsonProperty(required = true)
        private String format;
        @JsonProperty(required = true)
        private String regex;

        @JsonIgnore
        private Pattern pattern;
        @JsonProperty
        private boolean possibleMissingYear = false;
        @Nullable
        @JsonProperty
        private String yearSuffix;

        public String getFile() {
            return file;
        }

        public String getFormat() {
            return format;
        }

        public String getRegex() {
            return regex;
        }

        @JsonSetter
        public void setRegex(String regex) {
            this.regex = regex;
            this.pattern = Pattern.compile("^.*?(" + regex + ").*");
        }

        @JsonIgnore
        public Pattern getPattern() {
            return pattern;
        }

        public boolean getPossibleMissingYear() {
            return possibleMissingYear;
        }

        @Nullable
        public String getYearSuffix() {
            return yearSuffix;
        }
    }

}
