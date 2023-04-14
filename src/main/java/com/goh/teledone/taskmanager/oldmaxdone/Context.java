package com.goh.teledone.taskmanager.oldmaxdone;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Context {
    @SerializedName("id")
    private String id;

    @SerializedName("ct")
    private LocalDateTime ct;

    @SerializedName("mt")
    private LocalDateTime mt;

    @SerializedName("cb")
    private String cb;

    @SerializedName("owners")
    private List<String> owners;

    @SerializedName("title")
    private String title;

    @SerializedName("userId")
    private String userId;

    @SerializedName("archived")
    private Boolean archived;
}