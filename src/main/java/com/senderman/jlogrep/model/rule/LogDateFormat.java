package com.senderman.jlogrep.model.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senderman.jlogrep.exception.WrongDateRegexException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

public record LogDateFormat(DateFormatRule defaultRule, List<DateFormatRule> rules) {

    private static final ZoneOffset zoneOffset = ZoneOffset.UTC;

    public DateFormatRule getFormatFor(String fileName) {
        return rules.parallelStream()
                .filter(r -> fileName.contains(r.file))
                .findAny()
                .orElseGet(this::defaultRule);
    }

    public record DateFormatRule(
            String file,
            DateTimeFormatter format,
            boolean possibleMissingYear,
            String yearSuffix,
            Pattern pattern
    ) {
        @JsonIgnore
        public Instant extractDate(String line) throws DateTimeParseException, WrongDateRegexException {
            var matcher = pattern.matcher(line);
            if (!matcher.find())
                throw new WrongDateRegexException(line, pattern.pattern());
            try {
                String dateString = matcher.group(1);
                return LocalDateTime.parse(dateString, format).toInstant(zoneOffset);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("Failed to extract date for line %s with regex %s".formatted(line, pattern.pattern()), e);
            }
        }
    }

}
