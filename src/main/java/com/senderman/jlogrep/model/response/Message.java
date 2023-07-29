package com.senderman.jlogrep.model.response;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

@Serdeable
@Schema(description = "Error or info message")
public class Message {

    private final int code;
    private final String error;

    public Message(int code, String error) {
        this.code = code;
        this.error = error;
    }

    @Schema(description = "Response code")
    public int getCode() {
        return code;
    }

    @Schema(description = "message")
    public String getError() {
        return error;
    }
}
