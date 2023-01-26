package com.senderman.jlogrep.exception;

public class InvalidConfigFormatException extends BadRequestException {

    public InvalidConfigFormatException(String file) {
        super("invalid input format for " + file);
    }

}
