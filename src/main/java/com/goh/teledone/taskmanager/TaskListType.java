package com.goh.teledone.taskmanager;

import java.util.stream.Stream;

public enum TaskListType {
    INBOX, TODAY, WEEK, BACKLOG, DONE;

    public static Stream<TaskListType> stream() {
        return Stream.of(TaskListType.values());
    }
}
