package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            log.error("解析学生信息发生错误");
            throw new PageStructureException("学生信息页面结构已变化：未找到 .middletopttxlr 容器");
        }
        String name = null;
        String studentId = null;
        String academy = null;
        String major = null;
        String className = null;
        Elements rows = container.children();
        for (Element row : rows) {
            Element titleElement = row.selectFirst(".middletopdwxxtit");
            Element valueElement = row.selectFirst(".middletopdwxxcont");
            if (titleElement == null || valueElement == null) {
                throw new PageStructureException("学生信息页面结构已变化：缺少字段标题或内容节点");
            }
            String title = titleElement.text().trim();
            String value = valueElement.text().trim();
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
        if (name == null || studentId == null) {
            throw new ParsingErrorException("学生信息解析失败：缺少姓名或学号");
        }
        log.debug("解析学生信息成功: {} - {}", name, studentId);
        return new StudentInfo(name, studentId, academy, major, className);
    }
}
