package com.goh.teledone.stt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * todo.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VkRecognitionResponse {

    /**
     * todo.
     */
    @JsonProperty("response")
    private UploadURL response;

    /**
     * todo.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadURL {

        /**
         * todo.
         */
        @JsonProperty("upload_url")
        private String uploadUrl;

    }
}
