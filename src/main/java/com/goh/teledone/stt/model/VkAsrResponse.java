package com.goh.teledone.stt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO ответа от сервиса распознавания речи.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VkAsrResponse {

    /**
     * Ответ.
     */
    @JsonProperty("response")
    private TaskInfo response;

    /**
     * todo.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskInfo {

        /**
         * todo.
         */
        @JsonProperty("task_id")
        private String taskId;

    }
}
