package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * Created on 2026/1/3 14:28
 *
 * @author PlagueWZK
 */

@Slf4j
public class SjmsParser implements HtmlParser<String> {
    @Override
    public String parser(String html) {
        Element option = Jsoup.parse(html).selectFirst("select[name='sjms'] option[value]");
        if (option == null) {
            throw new PageStructureException("未找到sjms标签");
        }
        return option.val().trim();
    }
}
