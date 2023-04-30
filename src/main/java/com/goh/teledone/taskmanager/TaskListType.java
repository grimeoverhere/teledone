package com.goh.teledone.taskmanager;

import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

import static com.goh.teledone.taskmanager.TaskAction.*;

@Getter
public enum TaskListType {
    INBOX(List.of(MOVE_TODAY, MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG), List.of(MARK_DONE, EDIT, DELETE)),
    TODAY(List.of(MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG), List.of(MARK_DONE, EDIT, DELETE)),
    WEEK(List.of(MOVE_TODAY, MOVE_TO_BACKLOG), List.of(MARK_DONE, EDIT, DELETE)),
    BACKLOG(List.of(MOVE_TODAY, MOVE_TO_THIS_WEEK), List.of(MARK_DONE, EDIT, DELETE)),
    DONE(List.of(MOVE_TODAY, MOVE_TO_THIS_WEEK, MOVE_TO_BACKLOG), List.of(DELETE));

    private final List<TaskAction>[] availableActions;

    @SafeVarargs
    TaskListType(List<TaskAction>... actions) {
        this.availableActions = actions;
    }

    public static Stream<TaskListType> stream() {
        return Stream.of(TaskListType.values());
    }
}
