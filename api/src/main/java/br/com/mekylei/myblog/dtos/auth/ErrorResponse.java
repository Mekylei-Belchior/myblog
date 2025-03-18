package br.com.mekylei.myblog.dtos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record ErrorResponse(
        @JsonProperty("timestamp") String formattedTimestamp,
        int status,
        String error,
        String message,
        String path) {

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this(formatInstant(timestamp), status, error, message, path);
    }

    private static String formatInstant(Instant timestamp) {
        Locale systemLocale = Locale.getDefault();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", systemLocale)
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp);
    }

}
