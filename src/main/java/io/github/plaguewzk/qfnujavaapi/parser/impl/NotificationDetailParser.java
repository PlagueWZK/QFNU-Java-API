package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

/**
 * Created on 2026/1/2 00:51
 *
 * @author PlagueWZK
 */
@Slf4j
public class NotificationDetailParser implements HtmlParser<NotificationDetailParser.DetailResult> {

    @Override
    public DetailResult parser(String html) {
        Document doc = Jsoup.parse(html);
        String publisher = null;
        String dateTime = null;
        String content = null;
        String contentHtml = null;
        try {
            Element body = Objects.requireNonNull(doc.selectFirst("body"));
            Element desc = Objects.requireNonNull(body.selectFirst("p.desc"));
            Elements span = desc.select("span");
            publisher = !span.isEmpty() ? span.get(0).text().trim() : "未知发布者";
            dateTime = span.size() > 1 ? span.get(1).text().trim() : "未知时间";
            Element contentDiv = Objects.requireNonNull(body.selectFirst("div.content"));
            content = contentDiv.text();
            contentHtml = contentDiv.html();
        } catch (Exception e) {
            log.error("解析通知时发生错误", e);
        }
        return new DetailResult(publisher, dateTime, content, contentHtml);
    }

    public record DetailResult(String publisher, String dateTime, String content, String html) {
    }
}
