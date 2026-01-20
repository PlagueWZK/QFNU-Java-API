package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.SystemChangedException;
import io.github.plaguewzk.qfnujavaapi.model.entity.CourseInfo;
import io.github.plaguewzk.qfnujavaapi.model.entity.WeeklySchedule;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2026/1/2 23:17
 *
 * @author PlagueWZK
 */

@Slf4j
public class WeeklyScheduleParser implements HtmlParser<WeeklySchedule> {
    private final CourseInfoParser courseInfoParser = new CourseInfoParser();

    @Override
    public WeeklySchedule parser(String html) {
        Document doc = Jsoup.parse(html);
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) {
            throw new SystemChangedException("未找到课表tbody标签");
        }
        Elements sections = tbody.select("tbody tr");
        if (sections.isEmpty()) {
            log.warn("未查询到课表");
            return null;
        }
        List<CourseInfo> weeklySchedule = new ArrayList<>();
        for (Element section : sections) {
            Elements courseHtml = section.select("td p");
            if (courseHtml.isEmpty()) {
                continue;
            }
            for (Element element : courseHtml) {
                CourseInfo courseInfo = courseInfoParser.parser(element.html());
                weeklySchedule.add(courseInfo);
            }
        }
        return new WeeklySchedule(weeklySchedule.get(0).week(), weeklySchedule);
    }
}
