package io.github.plaguewzk.qfnujavaapi.model.notification;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationTest {

    @Test
    void shouldParseDateTimeWithSeconds() {
        Notification notification = new Notification("1", "", "title", "publisher", "2026/03/09 10:11:12", "", "", false);

        LocalDateTime publishTime = notification.publishTime();

        assertEquals(LocalDateTime.of(2026, 3, 9, 10, 11, 12), publishTime);
    }

    @Test
    void shouldParseDateTimeWithoutSeconds() {
        Notification notification = new Notification("1", "", "title", "publisher", "2026/03/09 10:11", "", "", false);

        LocalDateTime publishTime = notification.publishTime();

        assertEquals(LocalDateTime.of(2026, 3, 9, 10, 11), publishTime);
    }

    @Test
    void shouldReturnNullWhenDateTimeInvalid() {
        Notification notification = new Notification("1", "", "title", "publisher", "not-a-date", "", "", false);

        assertNull(notification.publishTime());
    }
}
