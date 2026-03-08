package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.entity.Course;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created on 2026/1/19 01:45
 *
 * @author PlagueWZK
 */
@SuppressWarnings("OptionalOfNullableMisuse")
@Slf4j
public class CourseParser implements HtmlParser<List<Course>> {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("-{4,}");

    /**
     * 解析课程列表（处理单个单元格内包含多门课的情况）
     *
     * @param html 包含课程信息的td标签html
     * @return 返回Course列表，如果没有课则返回空列表
     */
    @Override
    public List<Course> parser(String html) {
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
            Course course = parseSingleCourseFragment(segment);
            if (course != null) {
                courses.add(course);
            }
        }

        return courses;
    }

    /**
     * 解析单个课程片段
     *
     * @param htmlFragment 切割后的 HTML 片段
     */
    @Nullable
    private Course parseSingleCourseFragment(String htmlFragment) {
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
        Course.Weeks weeks = Optional.ofNullable(rawTime)
                .map(t -> t.split("\\[")[0])
                .map(t -> t.replace("(周)", ""))
                .map(String::trim)
                .map(Course.Weeks::parse)
                .orElse(null);

        Course.Section section = Optional.ofNullable(rawTime)
                .map(Course.Section::parse)
                .orElse(null);

        return new Course(courseName, weeks, section, location, teacher);
    }

    /**
     * 辅助提取方法
     */
    @Nullable
    private String extractText(Element parent, String title) {
        return Optional.ofNullable(parent.selectFirst("font[title='" + title + "']"))
                .map(Element::text)
                .map(String::trim)
                .orElse(null);
    }
}
