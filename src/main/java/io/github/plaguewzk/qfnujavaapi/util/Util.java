package io.github.plaguewzk.qfnujavaapi.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created on 2026/1/2 15:42
 *
 * @author PlagueWZK
 */

public final class Util {
    public static final java.time.format.DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static final java.time.format.DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER_IGNORE_SECOND =
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private Util() {
    }

    /**
     * URL 编码字符串，null 值编码为空字符串。
     */
    public static String encodeUrl(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    public static String cleanHtml(String rawHtml) {
        if (rawHtml == null || rawHtml.isEmpty()) {
            return "";
        }

        Safelist structureSafelist = Safelist.none()
                .addTags("table", "thead", "tbody", "tfoot", "tr", "th", "td")
                .addTags("div", "p", "br", "hr")
                .addTags("ul", "ol", "li", "dl", "dt", "dd")
                .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                .addTags("b", "strong", "i", "em");

        structureSafelist.addAttributes("td", "rowspan", "colspan");
        structureSafelist.addAttributes("th", "rowspan", "colspan");
        String cleanedHtml = Jsoup.clean(rawHtml, structureSafelist);
        Document doc = Jsoup.parseBodyFragment(cleanedHtml);
        doc.outputSettings().prettyPrint(true);
        return doc.body().html();
    }
}
