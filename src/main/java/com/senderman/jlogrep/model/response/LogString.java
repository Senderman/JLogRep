package com.senderman.jlogrep.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senderman.jlogrep.util.ComparableArrayDeque;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Deque;
import java.util.Objects;

@Serdeable
@Schema(description = "Info about matched lines")
public class LogString implements Comparable<LogString> {

    private final transient String file;
    private final Deque<String> lines;
    @Nullable
    private final Instant date;

    // Date will be saved as new Date(0) if null is given
    public LogString(String file, ComparableArrayDeque<String> lines, @Nullable Instant date) {
        this.file = file;
        this.lines = lines;
        this.date = date;
    }

    @JsonIgnore
    public String getFile() {
        return file;
    }

    @Schema(description = "Lines. May contain several lines due to MULTILINE rules / capturing groups")
    public Deque<String> getLines() {
        return lines;
    }

    @Nullable
    @JsonIgnore
    public Instant getRawDate() {
        return date;
    }

    @Nullable
    @Schema(description = "Date in unix time. UTC+0. Milliseconds. Null if unknown")
    public Long getDate() {
        return date == null ? null : date.toEpochMilli();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }

    @Override
    public int compareTo(LogString logString) {
        Instant o1Date = this.getRawDate();
        Instant o2Date = logString.getRawDate();

        // move all non-dated objects to the top
        if (o1Date == null) {
            if (o2Date == null) {
                return 0; // Both objects are null, consider them equal
            } else {
                return -1; // Only the current object is null, consider it less than the provided object
            }
        } else if (o2Date == null) {
            return 1; // Only the provided object is null, consider the current object greater
        }

        return o1Date.compareTo(o2Date);
    }
}
