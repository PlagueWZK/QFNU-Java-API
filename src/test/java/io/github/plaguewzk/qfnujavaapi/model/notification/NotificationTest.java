package io.github.plaguewzk.qfnujavaapi.model.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("通知模型")
class NotificationTest {

    @Test
    @DisplayName("解析带秒数的日期时间字符串")
    void shouldParseDateTimeWithSeconds() {
        Notification notification = new Notification("1", "", "title", "publisher", "2026/03/09 10:11:12", "", "", false);

        LocalDateTime publishTime = notification.publishTime();

        assertEquals(LocalDateTime.of(2026, 3, 9, 10, 11, 12), publishTime);
    }

    @Test
    @DisplayName("解析不带秒数的日期时间字符串")
    void shouldParseDateTimeWithoutSeconds() {
        Notification notification = new Notification("1", "", "title", "publisher", "2026/03/09 10:11", "", "", false);

        LocalDateTime publishTime = notification.publishTime();

        assertEquals(LocalDateTime.of(2026, 3, 9, 10, 11), publishTime);
    }

    @Test
    @DisplayName("无效日期时间字符串返回 null")
    void shouldReturnNullWhenDateTimeInvalid() {
        Notification notification = new Notification("1", "", "title", "publisher", "not-a-date", "", "", false);

        assertNull(notification.publishTime());
    }
}
