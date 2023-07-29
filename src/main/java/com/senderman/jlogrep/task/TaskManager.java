package com.senderman.jlogrep.task;

import com.senderman.jlogrep.exception.NoSuchTaskException;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Prototype
public class TaskManager<T> {

    private final Map<String, Task<T>> tasks;
    private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    public TaskManager() {
        this.tasks = new ConcurrentHashMap<>();
    }

    /**
     * Schedule a task to execute. Note that if you don't retrieve your task
     * using {@link TaskManager#getTaskAndDeleteIfReady} in 3 minutes after it has finished, it will be deleted
     *
     * @param action action to schedule
     * @return {@link TaskRepresentation} object
     */
    synchronized public TaskRepresentation<T> addTask(Supplier<T> action) {
        String taskId;
        // generate random uuid
        do {
            taskId = UUID.randomUUID().toString();
        } while (tasks.containsKey(taskId));

        var task = new Task<>(taskId, CompletableFuture.supplyAsync(action));
        tasks.put(taskId, task);
        return new TaskRepresentation<>(task);
    }

    /**
     * Get task by task id
     *
     * @param taskId id of the task
     * @return the {@link TaskRepresentation} object made from {@link Task} associated with the given id
     * @throws NoSuchTaskException if there's no task associated with the given id
     */
    synchronized public TaskRepresentation<T> getTaskAndDeleteIfReady(String taskId) throws NoSuchTaskException {
        var task = tasks.get(taskId);
        if (task == null)
            throw new NoSuchTaskException(taskId);

        var taskRepresentation = new TaskRepresentation<>(task);
        if (taskRepresentation.isReady())
            tasks.remove(taskId);

        return taskRepresentation;
    }

    @Scheduled(fixedDelay = "1m")
    synchronized void cleanOldTasks() {
        Deque<String> tasksToRemove = new ArrayDeque<>();
        for (var task : tasks.values()) {
            if (!task.getFuture().isDone())
                continue;

            if (task.getTimesVisited() == 2) {
                logger.warn("removing unrequested task " + task.getTaskId());
                tasksToRemove.add(task.getTaskId());
            } else
                task.incTimesVisited();
        }
        tasksToRemove.forEach(tasks::remove);
    }
}
