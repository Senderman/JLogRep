package com.senderman.jlogrep.task;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.concurrent.ExecutionException;

// This is made to avoid possible race conditions during Task serialization
@Serdeable
@Schema
public class TaskRepresentation<T> {

    private final String taskId;
    private final boolean isReady;
    @Nullable
    private final T result;

    public TaskRepresentation(Task<T> task) {
        this.taskId = task.getTaskId();
        this.isReady = task.getFuture().isDone();
        if (!isReady) {
            this.result = null;
        } else {
            try {
                this.result = task.getFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                var msg = "Exception occurred while getting future for task %s: %s".formatted(taskId, e.getMessage());
                throw new RuntimeException(msg, e);
            }
        }
    }

    @Schema(description = "Id of the task")
    public String getTaskId() {
        return taskId;
    }

    @Schema(description = "Is task ready")
    public boolean isReady() {
        return isReady;
    }

    @Nullable
    @Schema(description = "Result of the task. Null if task is not ready")
    public T getResult() {
        return result;
    }
}
