package com.goh.teledone.taskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task implements Serializable {
    private Long id;
    private ZonedDateTime createDate;
    private ZonedDateTime modifyDate;
    private ZonedDateTime dueDate;
    private ZonedDateTime startDatetime;
    private ZonedDateTime completionDate;
    private String categoryId;
    private String title;
    private String taskType;
    private boolean done;
    private String notes;
    private String fileId;
//    private List<ChecklistItem> checklistItems;
}

