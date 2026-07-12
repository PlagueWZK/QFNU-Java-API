package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationCourse;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import io.github.plaguewzk.qfnujavaapi.parser.ParserUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 解析学生评教课程列表页面（xspj_list.do）。
 *
 * @author PlagueWZK
 */
public class EvaluationCourseParser implements HtmlParser<List<EvaluationCourse>> {

    private static final Pattern JX02ID_PATTERN = Pattern.compile("jx02id=([^&]+)");
    private static final Pattern JX0404ID_PATTERN = Pattern.compile("jx0404id=([^&]+)");
    private static final Pattern XSFLID_PATTERN = Pattern.compile("xsflid=([^&]+)");
    private static final Pattern JG0101ID_PATTERN = Pattern.compile("jg0101id=([^&]+)");
    private static final Pattern ZPF_PATTERN = Pattern.compile("zpf=([^&]+)");

    private static final int COLUMN_COUNT = 18;

    @Override
    public List<EvaluationCourse> parser(String html) {
        if (html == null || html.isBlank()) {
            throw new PageStructureException("学生评教课程列表页面为空，无法解析");
        }

        Document document = Jsoup.parse(html);
        Element table = document.selectFirst("table#dataList");
        if (table == null) {
            throw new PageStructureException("学生评教课程列表页面结构变化: 未找到 table#dataList");
        }

        Elements rows = table.select("tr:has(> td)");
        List<EvaluationCourse> courses = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Elements cells = rows.get(rowIndex).select("> td");
            if (ParserUtils.isNoDataRow(cells)) {
                continue;
            }
            if (cells.size() < COLUMN_COUNT) {
                throw new ParsingErrorException(
                        "解析评教课程列表时字段缺失: 第 " + (rowIndex + 1) + " 行, 期望至少 " + COLUMN_COUNT
                                + " 列, 实际 " + cells.size() + " 列"
                );
            }

            // 基础字段
            String index = ParserUtils.getCellText(cells, 0, rowIndex, "序号");
            String courseId = ParserUtils.getCellText(cells, 1, rowIndex, "课程编号");
            String courseName = ParserUtils.getCellText(cells, 2, rowIndex, "课程名称");
            String teacher = ParserUtils.getCellText(cells, 3, rowIndex, "授课教师");
            String evalCategory = ParserUtils.getCellText(cells, 4, rowIndex, "评教类别");
            String totalScore = ParserUtils.getCellText(cells, 5, rowIndex, "总评分");
            String evaluated = ParserUtils.getCellText(cells, 6, rowIndex, "已评");
            String submitted = ParserUtils.getCellText(cells, 7, rowIndex, "是否提交");

            // 学时字段 (列索引 8-16)
            String lectureHours = ParserUtils.getCellText(cells, 8, rowIndex, "讲课学时");
            String practiceHours = ParserUtils.getCellText(cells, 9, rowIndex, "实践学时");
            String seminarHours = ParserUtils.getCellText(cells, 10, rowIndex, "讲座学时");
            String experimentHours = ParserUtils.getCellText(cells, 11, rowIndex, "实验学时");
            String designHours = ParserUtils.getCellText(cells, 12, rowIndex, "设计学时");
            String computerHours = ParserUtils.getCellText(cells, 13, rowIndex, "其中上机学时");
            String discussionHours = ParserUtils.getCellText(cells, 14, rowIndex, "讨论辅导学时");
            String extracurricularHours = ParserUtils.getCellText(cells, 15, rowIndex, "课外学时");
            String onlineHours = ParserUtils.getCellText(cells, 16, rowIndex, "网络学时");

            // 操作列（第 18 列，索引 17）
            Element link = cells.get(17).selectFirst("a[href]");
            String evalUrl = link != null ? link.attr("href") : "";
            String href = evalUrl;

            String jx02id = ParserUtils.extractUrlParam(href, JX02ID_PATTERN);
            String jx0404id = ParserUtils.extractUrlParam(href, JX0404ID_PATTERN);
            String xsflid = ParserUtils.extractUrlParam(href, XSFLID_PATTERN);
            String jg0101id = ParserUtils.extractUrlParam(href, JG0101ID_PATTERN);
            String zpf = ParserUtils.extractUrlParam(href, ZPF_PATTERN);

            courses.add(new EvaluationCourse(
                    index, courseId, courseName, teacher, evalCategory,
                    totalScore, evaluated, submitted,
                    lectureHours, practiceHours, seminarHours, experimentHours,
                    designHours, computerHours, discussionHours, extracurricularHours,
                    onlineHours, evalUrl,
                    jx02id, jx0404id, xsflid, jg0101id, zpf
            ));
        }
        return courses;
    }
}
