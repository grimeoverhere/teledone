package com.goh.teledone.stt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Todo.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VkAsrResult {

    /**
     * todo.
     */
    @JsonProperty("response")
    private TextResponse response;

    /**
     * todo.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextResponse {

        /**
         * todo.
         */
        private String id;

        /**
         * todo.
         */
        private String status;

        /**
         * todo.
         */
        private String text;

    }
}
