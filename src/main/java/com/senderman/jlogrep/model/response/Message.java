package com.senderman.jlogrep.model.response;

public class Message {

    private final int code;
    private final String error;

    public Message(int code, String error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }
}
