package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.Objects;

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
        return Objects.requireNonNull(option).val().trim();
    }
}
