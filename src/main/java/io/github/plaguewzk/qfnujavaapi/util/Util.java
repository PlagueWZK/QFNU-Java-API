package io.github.plaguewzk.qfnujavaapi.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.time.format.DateTimeFormatter;

/**
 * Created on 2026/1/2 15:42
 *
 * @author PlagueWZK
 */

public class Util {
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER_IGNORE_SECOND = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private Util() {
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
