package br.com.mekylei.myblog.dtos.auth;

import br.com.mekylei.myblog.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ErrorResponse(
        @JsonProperty("timestamp") String timestamp,
        int status,
        String error,
        String message,
        String path) {

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this(DateUtil.formatInstant(timestamp), status, error, message, path);
    }

}
