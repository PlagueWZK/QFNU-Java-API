package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.notification.NotificationDetail;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import io.github.plaguewzk.qfnujavaapi.util.Util;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

/**
 * Created on 2026/1/2 00:51
 *
 * @author PlagueWZK
 */
public class NotificationDetailParser implements HtmlParser<NotificationDetail> {

    @Override
    public NotificationDetail parser(String html) {
        Document doc = Jsoup.parse(html);

        Optional<Element> bodyOpt = Optional.of(doc.body());
        Optional<Elements> span = bodyOpt.map(body -> body.selectFirst("p.desc"))
                .map(desc -> desc.select("span"));

        String publisher = span.filter(spans -> !spans.isEmpty())
                .map(spans -> spans.get(0).text().trim())
                .orElse("未知发布者");
        String dateTime = span.filter(spans -> spans.size() > 1)
                .map(spans -> spans.get(1).text().trim())
                .orElse("未知时间");

        Optional<Element> contentOpt = bodyOpt.map(body -> body.selectFirst("div.content"));
        String content = contentOpt.map(Element::text).orElse("");
        String contentHtml = contentOpt.map(element -> Util.cleanHtml(element.html())).orElse("");
        return new NotificationDetail(publisher, dateTime, content, contentHtml);
    }
}
