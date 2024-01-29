package com.goh.teledone.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;
    private String localId;
    private String createDate;
    private String modifyDate;
    private String title;
    private String notes;
    private String type;
    private boolean done;

}
