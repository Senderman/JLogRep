package com.senderman.jlogrep.exception;

public class InvalidConfigFormatException extends BadRequestException {

    public InvalidConfigFormatException(String file, String reason) {
        super("invalid input format for file %s, reason: %s".formatted(file, reason));
    }

    public InvalidConfigFormatException(String file, Throwable cause) {
        super("invalid input format for %s: %s".formatted(file, cause.getMessage()), cause);
    }
}
