package com.senderman.jlogrep.exception;

public class EmptyBugreportNameException extends BadRequestException {

    public EmptyBugreportNameException() {
        super("Unable to get bugreport filename, please use alternative browser");
    }
}
