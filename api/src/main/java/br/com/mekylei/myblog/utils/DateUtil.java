package br.com.mekylei.myblog.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static String formatInstant(Instant timestamp) {
        Locale systemLocale = Locale.getDefault();
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", systemLocale)
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp);
    }

    public static String formatLocalDateTime(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                .withZone(ZoneId.systemDefault());
        return formatter.format(timestamp.atZone(ZoneId.systemDefault()));
    }

    public static String formatDateTime(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return formatter.format(timestamp);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(Instant instant) {
        return Date.from(instant);
    }

}
