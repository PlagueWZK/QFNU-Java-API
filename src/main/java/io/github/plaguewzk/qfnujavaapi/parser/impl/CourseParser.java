package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.course.Course;
import io.github.plaguewzk.qfnujavaapi.model.course.Section;
import io.github.plaguewzk.qfnujavaapi.model.course.Weekday;
import io.github.plaguewzk.qfnujavaapi.model.course.Weeks;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created on 2026/1/19 01:45
 *
 * @author PlagueWZK
 */
@Slf4j
public class CourseParser implements HtmlParser<List<Course>> {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("-{4,}");
    private static final Pattern WEEKS_SUFFIX_PATTERN = Pattern.compile("\\(周\\)?\\s*$");

    @Override
    public List<Course> parser(String html) {
        return parser(html, Weekday.UNDEFINED);
    }

    public List<Course> parser(String html, Weekday weekday) {
        if (html == null || html.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Document doc = Jsoup.parseBodyFragment(html);
        Element contentDiv = doc.selectFirst(".kbcontent");
        if (contentDiv == null) {
            return new ArrayList<>();
        }

        String innerHtml = contentDiv.html();
        String[] courseSegments = SPLIT_PATTERN.split(innerHtml);

        List<Course> courses = new ArrayList<>();
        for (String segment : courseSegments) {
            if (segment.replaceAll("<br>|&nbsp;", "").trim().isEmpty()) {
                continue;
            }
            Course course = parseSingleCourseFragment(segment, weekday);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }

    @Nullable
    private Course parseSingleCourseFragment(String htmlFragment, Weekday weekday) {
        Document doc = Jsoup.parseBodyFragment("<div>" + htmlFragment + "</div>");
        Element container = doc.body().child(0);

        String courseName = null;
        for (TextNode node : container.textNodes()) {
            String text = node.text().trim();
            if (text.length() > 1) {
                courseName = text;
                break;
            }
        }

        if (courseName == null || courseName.trim().isEmpty()) {
            return null;
        }

        String teacher = extractText(container, "老师");
        String location = extractText(container, "教室");
        String rawTime = extractText(container, "周次(节次)");
        if (rawTime == null || rawTime.isBlank()) {
            throw new ParsingErrorException("课程解析失败：缺少周次(节次)信息，课程名=" + courseName);
        }

        Weeks weeks = Optional.of(rawTime)
                .map(text -> text.split("\\[")[0])
                .map(text -> WEEKS_SUFFIX_PATTERN.matcher(text).replaceFirst(""))
                .map(String::trim)
                .map(Weeks::parse)
                .orElse(null);

        Section section = Optional.of(rawTime)
                .map(Section::parse)
                .orElse(null);

        return new Course(Objects.requireNonNullElse(weekday, Weekday.UNDEFINED), courseName, weeks, section, location, teacher);
    }

    @Nullable
    private String extractText(Element parent, String title) {
        return Optional.ofNullable(parent.selectFirst("font[title='" + title + "']"))
                .map(Element::text)
                .map(String::trim)
                .orElse(null);
    }
}
