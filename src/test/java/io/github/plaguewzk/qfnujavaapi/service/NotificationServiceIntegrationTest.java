package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.BaseIntegrationTest;
import io.github.plaguewzk.qfnujavaapi.model.notification.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationService 集成测试，使用真实教务系统数据。
 *
 * @author PlagueWZK
 */
@DisplayName("通知公告服务集成测试")
class NotificationServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("获取通知公告列表并填充详情")
    void shouldGetNotificationList() {
        NotificationService service = client.service(NotificationService.class);
        List<Notification> notifications = service.getList();

        assertNotNull(notifications, "通知列表不应为 null");
        // 可能为空（无通知），不强制要求非空
        for (Notification notification : notifications) {
            assertNotNull(notification.title(), "通知标题不应为 null");
            assertTrue(notification.loaded(), "通知详情应已加载");
        }
    }
}
