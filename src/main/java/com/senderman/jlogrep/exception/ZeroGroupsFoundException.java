package com.senderman.jlogrep.exception;

public class ZeroGroupsFoundException extends BadRequestException {

    public ZeroGroupsFoundException(String line, String pattern) {
        super(String.format("No groups captured for line %s with pattern %s", line, pattern));
    }

}
