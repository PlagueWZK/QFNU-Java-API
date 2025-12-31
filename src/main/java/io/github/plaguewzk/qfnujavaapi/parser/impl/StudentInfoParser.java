package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.entity.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

/**
 * Created on 2025/12/31 00:21
 *
 * @author PlagueWZK
 */

@Slf4j
public class StudentInfoParser implements HtmlParser<StudentInfo> {

    @Override
    public StudentInfo parser(String html) {
        Document doc = Jsoup.parse(html);
        Element container = doc.selectFirst(".middletopttxlr");
        if (container == null) {
            log.warn("教务系统HTML源码变更，获取信息失败");
            return null;
        }
        String name = null;
        String studentId = null;
        String academy = null;
        String major = null;
        String className = null;
        Elements rows = container.children();
        for (Element row : rows) {
            String title = Objects.requireNonNull(row.selectFirst(".middletopdwxxtit")).text().trim();
            String value = Objects.requireNonNull(row.selectFirst(".middletopdwxxcont")).text().trim();
            if (title.contains("姓名")) {
                name = value;
            } else if (title.contains("编号")) {
                studentId = value;
            } else if (title.contains("院系")) {
                academy = value;
            } else if (title.contains("专业")) {
                major = value;
            } else if (title.contains("班级")) {
                className = value;
            }
        }
        log.debug("解析学生信息成功: {} - {}", name, studentId);
        return new StudentInfo(name, studentId, academy, major, className);
    }
}
