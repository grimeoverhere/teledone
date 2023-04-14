package com.goh.teledone.taskmanager.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @SerializedName("id")
    private String id;

    @SerializedName("ct")
    private LocalDateTime ct;

    @SerializedName("mt")
    private LocalDateTime mt;

    @SerializedName("cb")
    private String cb;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;
}