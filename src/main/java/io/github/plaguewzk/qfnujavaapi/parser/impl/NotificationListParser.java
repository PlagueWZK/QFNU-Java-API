package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.entity.Notification;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2026/1/2 16:49
 *
 * @author PlagueWZK
 */

@Slf4j
public class NotificationListParser implements HtmlParser<List<Notification>> {
    @Override
    public List<Notification> parser(String html) {
        Document doc = Jsoup.parse(html);
        Elements notifications = doc.select("li.list-group-item");
        List<Notification> notificationList = new LinkedList<>();
        try {
            for (Element notificationEle : notifications) {
                String title = notificationEle.attr("title");
                Element a = Objects.requireNonNull(notificationEle.selectFirst("a[onclick]"));
                String link = a.html();
                String onclick = a.attr("onclick");
                String id = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
                String dateTime = notificationEle.select("span[id^=fbsj]").text().trim();
                notificationList.add(new Notification(id, link, title, null, dateTime, null, null, false));
            }
        } catch (Exception e) {
            log.error("解析通知列表时出错", e);
        }
        return notificationList;
    }
}
