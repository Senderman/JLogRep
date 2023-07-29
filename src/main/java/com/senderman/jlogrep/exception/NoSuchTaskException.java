package com.senderman.jlogrep.exception;

public class NoSuchTaskException extends RuntimeException {

    public NoSuchTaskException(String taskId) {
        super("No task found with id " + taskId);
    }

}
