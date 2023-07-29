package com.senderman.jlogrep.model.request;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.format.Format;

import java.util.Collections;
import java.util.Set;

public class ScanRequest extends AbsAnalysisRequest {

    private final Set<String> tags;

    public ScanRequest(
            @Nullable @Format(DATE_FORMAT) String date,
            @Nullable Integer year,
            @Nullable Integer interval,
            @Nullable String filters,
            @Nullable String tags
    ) {
        super(date, year, interval, filters);

        if (tags == null)
            this.tags = Collections.emptySet();
        else
            this.tags = Set.of(tags.split(","));
    }

    public Set<String> getTags() {
        return tags;
    }

}
