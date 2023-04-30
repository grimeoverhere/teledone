package com.goh.teledone.taskmanager;

import lombok.Getter;

import java.util.stream.Stream;

import static com.goh.teledone.taskmanager.TaskAction.*;

@Getter
public enum TaskListType {
    INBOX(MOVE_TODAY, MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG, MARK_DONE, EDIT, DELETE),
    TODAY(MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG, MARK_DONE, EDIT, DELETE),
    WEEK(MOVE_TODAY, MOVE_TO_BACKLOG, MARK_DONE, EDIT, DELETE),
    BACKLOG(MOVE_TODAY, MOVE_TO_THIS_WEEK, MARK_DONE, EDIT, DELETE),
    DONE(MOVE_TODAY, MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG, DELETE);

    private final TaskAction[] availableActions;

    TaskListType(TaskAction... actions) {
        this.availableActions = actions;
    }

    public static Stream<TaskListType> stream() {
        return Stream.of(TaskListType.values());
    }
}
