package com.rexijie.webflixstreamingservice.model.helpers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
    @JsonProperty("time")
    public String timestamp;
    public String error;
    public Integer status;

    @Override
    public String toString() {
        return "{" +
                "\"timestamp\":\"" + timestamp + '\"' +
                ", \"error\":\"" + error + '\"' +
                ", \"status\":" + status +
                '}';
    }
}
