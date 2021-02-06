package me.vaxry.harakiri.framework.task;

import me.vaxry.harakiri.framework.task.basic.BasicTask;

import java.util.List;

public interface TaskFactory<T extends BasicTask> {

    void removeTask(String taskName);

    void removeTask(T task);

    List<T> getTasks();
}
