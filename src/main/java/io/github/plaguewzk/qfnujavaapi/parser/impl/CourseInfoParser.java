package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.entity.CourseInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2026/1/18 16:25
 *
 * @author PlagueWZK
 */

@Slf4j
public class CourseInfoParser implements HtmlParser<CourseInfo> {
    @Override
    public CourseInfo parser(String html) {
        if (html == null || html.isBlank()) {
            throw new ParsingErrorException("课程详情解析失败：HTML 不能为空");
        }
        String credits = null;
        String property = null;
        String courseName = null;
        String rawTime = null;
        String location = null;
        String className = null;
        Integer dayOfWeek = null;
        Integer startNode = null;
        Integer endNode = null;
        Integer week = null;

        Element courseHtml = Jsoup.parseBodyFragment(html);
        if (courseHtml.attr("title").isBlank()) {
            throw new ParsingErrorException("课程详情解析失败：缺少 title 属性");
        }

        String[] info = courseHtml.attr("title").split("<br/>");
        for (String s : info) {
            if (s.contains("学分")) {
                credits = s.substring(s.indexOf('：') + 1);
            } else if (s.contains("属性")) {
                property = s.substring(s.indexOf('：') + 1);
            } else if (s.contains("课程名称")) {
                courseName = s.substring(s.indexOf('：') + 1);
            } else if (s.contains("时间")) {
                rawTime = s.substring(s.indexOf('：') + 1);
            } else if (s.contains("地点")) {
                location = s.substring(s.indexOf('：') + 1);
            } else if (s.contains("课堂名称")) {
                className = s.substring(s.indexOf('：') + 1);
            }
            if (rawTime != null) {
                //第10周 星期二 [09-10-11]节
                String regex = "^第(\\d+)周\\s+星期([一二三四五六七])\\s+\\[(\\d+).*-(\\d+)]节$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(rawTime);
                if (matcher.find()) {
                    String weekStr = matcher.group(1);
                    String dayStr = matcher.group(2);
                    String startStr = matcher.group(3);
                    String endStr = matcher.group(4);
                    week = Integer.parseInt(weekStr);
                    dayOfWeek = parseWeekDay(dayStr);
                    startNode = Integer.parseInt(startStr);
                    endNode = Integer.parseInt(endStr);
                } else {
                    throw new ParsingErrorException("课程详情解析失败：时间格式无法识别 -> " + rawTime);
                }
            }
        }
        CourseInfo courseInfo = new CourseInfo(courseName, credits, property, className, location, rawTime, dayOfWeek, startNode, endNode, week);
        log.debug("课程解析成功：{}",courseInfo);
        return courseInfo;
    }

    private Integer parseWeekDay(String dayStr) {
        return switch (dayStr) {
            case "一" -> 1;
            case "二" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            case "七" -> 7;
            default -> 0;
        };
    }
}
