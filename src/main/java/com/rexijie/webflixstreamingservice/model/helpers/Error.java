package com.rexijie.webflixstreamingservice.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
    @JsonProperty("time")
    private String timestamp;
    private String err;
    private String message;
    private Integer status;
    private String path;

    @Override
    public String toString() {
        return "{" +
                "\"status\":" + status +
                ",\"timestamp\":\"" + timestamp + '\"' +
                ",\"error\":\"" + err + '\"' +
                ",\"message\":\"" + message + '\"' +
                ",\"path\": \"" + path + '\"' +
                '}';
    }
}
