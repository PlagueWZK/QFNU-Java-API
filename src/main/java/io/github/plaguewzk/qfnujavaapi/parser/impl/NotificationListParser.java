package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.entity.Notification;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Elements notificationsHtml = doc.select("li.list-group-item");
        List<Notification> notifications = new ArrayList<>();
        for (Element notificationEle : notificationsHtml) {
            String title = notificationEle.attr("title");
            Optional<Element> e = Optional.ofNullable(notificationEle.selectFirst("a[onclick]"));
            String link = e.map(Element::html).orElse("");
            String onclick = e.map(ee -> ee.attr("onclick")).orElse("");
            String id = null;
            if (!onclick.isBlank() && onclick.contains("'")) {
                id = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
            } else {
                log.warn("解析列表遇到无效条目(缺少ID), onclick内容: [{}], 标题: [{}]", onclick, title);
            }
            String dateTime = Optional.ofNullable(notificationEle.selectFirst("span[id^=fbsj]")).map(ee -> ee.text().trim()).orElse("");
            notifications.add(new Notification(id, link, title, null, dateTime, null, null, false));
        }
        return notifications;
    }
}
