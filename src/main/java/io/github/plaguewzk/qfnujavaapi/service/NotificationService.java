package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.entity.Notification;
import io.github.plaguewzk.qfnujavaapi.parser.impl.NotificationDetailParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.NotificationListParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on 2026/1/2 00:50
 *
 * @author PlagueWZK
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final QFNUExecutor qfnuExecutor;
    private final NotificationDetailParser notificationDetailParser = new NotificationDetailParser();
    private final NotificationListParser notificationListParser = new NotificationListParser();

    public List<Notification> getList() {
        String original = qfnuExecutor.executeGet(QFNUAPI.MAIN_INDEX_NOTIFICATION_LIST);
        List<Notification> list = notificationListParser.parser(original);
        List<Notification> filledList = new ArrayList<>();
        for (Notification notification : list) {
            filledList.add(fillDetail(notification));
        }
        return filledList;
    }

    public Notification fillDetail(Notification notification) {
        if (notification == null) {
            return null;
        }
        if (notification.loaded()) {
            return notification;
        }
        String id = notification.id();
        if (id == null) {
            log.warn("通知[title={}] id为null，跳过详情获取", notification.title());
            return notification;
        }
        String original = qfnuExecutor.executeGet(QFNUAPI.MAIN_INDEX_NOTIFICATION, Map.of("id", id));
        NotificationDetailParser.DetailResult detailResult = notificationDetailParser.parser(original);
        return notification.withDetails(detailResult.publisher(), detailResult.dateTime(), detailResult.content(), detailResult.html());
    }
}
