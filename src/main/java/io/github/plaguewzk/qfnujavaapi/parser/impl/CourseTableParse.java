package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.exception.SystemChangedException;
import io.github.plaguewzk.qfnujavaapi.model.entity.Course;
import io.github.plaguewzk.qfnujavaapi.model.entity.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.entity.Term;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 2026/1/19 01:46
 *
 * @author PlagueWZK
 */

public class CourseTableParse implements HtmlParser<CourseTable> {
    private final CourseParser courseParser = new CourseParser();

    @Override
    public CourseTable parser(String html) {
        if (!html.contains("学期理论课表")) {
            throw new SystemChangedException("未找到学期理论课表");
        }
        Document doc = Jsoup.parse(html);
        Elements courseTable = doc.select("table#kbtable td:has(div.kbcontent)");
        Set<Course> courses = new LinkedHashSet<>();
        for (Element element : courseTable) {
            List<Course> result = courseParser.parser(element.html());
            courses.addAll(result);
        }
        int week;
        Element selectedWeek = doc.selectFirst("select#zc option[selected]");
        if (selectedWeek == null || selectedWeek.val().isEmpty()) {
            week = 0;
        } else {
            try {
                week = Integer.parseInt(selectedWeek.val());
            } catch (NumberFormatException e) {
                throw new ParsingErrorException("学期理论课表解析失败：周次格式非法", e);
            }
        }
        Element selectedTerm = doc.selectFirst("select#xnxq01id option[selected]");
        Term term = Optional.ofNullable(selectedTerm).map(Element::text).map(Term::parse).orElse(null);
        return new CourseTable(term, week, new ArrayList<>(courses));
    }
}
