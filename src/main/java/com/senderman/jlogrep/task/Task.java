package com.senderman.jlogrep.task;

import java.util.concurrent.Future;

public class Task<T> {

    private final String taskId;
    private final Future<T> result;
    private int timesVisited = 0;

    public Task(String taskId, Future<T> result) {
        this.taskId = taskId;
        this.result = result;
    }

    public String getTaskId() {
        return taskId;
    }

    public Future<T> getFuture() {
        return result;
    }

    synchronized void incTimesVisited() {
        this.timesVisited++;
    }

    synchronized int getTimesVisited() {
        return timesVisited;
    }
}
