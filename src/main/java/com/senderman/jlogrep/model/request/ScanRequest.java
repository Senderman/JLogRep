package com.senderman.jlogrep.model.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.micronaut.core.annotation.Nullable;

import java.util.Set;

public class ScanRequest extends AbsAnalysisRequest {

    @Nullable
    private Set<String> tags;

    @Nullable
    public Set<String> getTags() {
        return tags;
    }

    @JsonSetter
    public void setTags(@Nullable String tags) {
        if (tags == null)
            this.tags = null;
        else
            this.tags = Set.of(tags.split(","));
    }

}
