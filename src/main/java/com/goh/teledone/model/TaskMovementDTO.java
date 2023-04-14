package com.goh.teledone.model;

import com.goh.teledone.taskmanager.TaskListType;

import java.io.Serializable;

public record TaskMovementDTO(Long taskId, TaskListType taskListType) implements Serializable {}
