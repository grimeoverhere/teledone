package com.goh.teledone.model;

import com.goh.teledone.taskmanager.TaskAction;

import java.io.Serializable;

public record TaskMovementDTO(Long taskId, TaskAction taskAction) implements Serializable {}
