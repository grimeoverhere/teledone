package com.goh.teledone.taskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task implements Serializable {
    private Long id;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private LocalDateTime dueDate;
    private LocalDateTime startDatetime;
    private LocalDateTime completionDate;
    private String categoryId;
    private String title;
    private String taskType;
    private boolean done;
    private String notes;
    private String fileId;
//    private List<ChecklistItem> checklistItems;
}

