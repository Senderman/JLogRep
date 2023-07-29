package com.senderman.jlogrep.exception;

public class WrongDateRegexException extends Exception {

    public WrongDateRegexException(String line, String pattern) {
        super(String.format("Wrong regex exception for line %s and pattern %s", line, pattern));
    }

}
