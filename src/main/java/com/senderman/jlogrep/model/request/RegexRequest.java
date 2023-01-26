package com.senderman.jlogrep.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegexRequest extends AbsAnalysisRequest {

    @JsonProperty(required = true)
    String regex;

    String file = "*";

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
