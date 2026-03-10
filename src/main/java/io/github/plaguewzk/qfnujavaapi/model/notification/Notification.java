package io.github.plaguewzk.qfnujavaapi.model.notification;

import io.github.plaguewzk.qfnujavaapi.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
public record Notification(String id,
                           String linkContent,
                           String title,
                           String publisher,
                           String datetime,
                           String content,
                           String html,
                           boolean loaded) {
    @Nullable
    public LocalDateTime publishTime() {
        if (datetime == null || datetime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(datetime, Util.DEFAULT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            try {
                return LocalDateTime.parse(datetime, Util.DEFAULT_DATE_TIME_FORMATTER_IGNORE_SECOND);
            } catch (DateTimeException ignored) {
                log.error("无法解析通知时间: {}", datetime, exception);
            }
        } catch (DateTimeException exception) {
            log.error("无法解析通知时间", exception);
        }
        return null;
    }

    public Notification withDetails(String publisher, String fullDateTime, String newContent, String newHtml) {
        return new Notification(this.id, this.linkContent, this.title, publisher, fullDateTime, newContent, newHtml, true);
    }
}
