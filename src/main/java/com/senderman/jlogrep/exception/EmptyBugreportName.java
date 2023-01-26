package com.senderman.jlogrep.exception;

public class EmptyBugreportName extends BadRequestException {

    public EmptyBugreportName() {
        super("Unable to get bugreport filename, please use alternative browser");
    }
}
