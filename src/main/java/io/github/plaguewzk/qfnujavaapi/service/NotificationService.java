package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.notification.Notification;
import io.github.plaguewzk.qfnujavaapi.model.notification.NotificationDetail;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2026/1/2 00:50
 *
 * @author PlagueWZK
 */
@Slf4j
public class NotificationService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<NotificationDetail> notificationDetailParser;
    private final HtmlParser<List<Notification>> notificationListParser;

    public NotificationService(
            QFNUContext context,
            HtmlParser<List<Notification>> notificationListParser,
            HtmlParser<NotificationDetail> notificationDetailParser
    ) {
        this.qfnuExecutor = context.executor();
        this.notificationListParser = Objects.requireNonNull(notificationListParser, "notificationListParser");
        this.notificationDetailParser = Objects.requireNonNull(notificationDetailParser, "notificationDetailParser");
    }

    public List<Notification> getList() {
        String original = qfnuExecutor.executeGet(QFNUAPI.MAIN_INDEX_NOTIFICATION_LIST);
        List<Notification> list = notificationListParser.parser(original);
        List<Notification> filledList = new ArrayList<>();
        for (Notification notification : list) {
            filledList.add(fillDetail(notification));
        }
        return filledList;
    }

    @Nullable
    public Notification fillDetail(@Nullable Notification notification) {
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
        NotificationDetail detailResult = notificationDetailParser.parser(original);
        return notification.withDetails(detailResult.publisher(), detailResult.dateTime(), detailResult.content(), detailResult.html());
    }
}
