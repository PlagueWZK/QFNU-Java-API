package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.exam.ExamSchedule;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2026/4/22 16:42
 *
 * @author PlagueWZK
 */

public class ExamScheduleParser implements HtmlParser<List<ExamSchedule>> {
    private static final int COLUMN_COUNT = 12;

    @Override
    public List<ExamSchedule> parser(String html) {
        if (html == null || html.isBlank()) {
            throw new PageStructureException("考试安排页面为空,无法解析");
        }

        Document document = Jsoup.parse(html);
        Element table = document.selectFirst("table#dataList");
        if (table == null) {
            throw new PageStructureException("考试安排页面结构变化: 未找到 table#dataList");
        }

        Elements rows = table.select("tr:has(> td)");
        List<ExamSchedule> schedules = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Elements cells = rows.get(rowIndex).select("> td");
            if (isNoDataRow(cells)) {
                continue;
            }
            if (cells.size() < COLUMN_COUNT) {
                throw new ParsingErrorException(
                        "解析考试安排时字段缺失: 第 " + (rowIndex + 1) + " 行, 期望至少 " + COLUMN_COUNT + " 列, 实际 " + cells.size() + " 列"
                );
            }

            schedules.add(new ExamSchedule(
                    getCellText(cells, 0, rowIndex, "序号"),
                    getCellText(cells, 1, rowIndex, "校区"),
                    getCellText(cells, 2, rowIndex, "考试场次"),
                    getCellText(cells, 3, rowIndex, "课程编号"),
                    getCellText(cells, 4, rowIndex, "课程名称"),
                    getCellText(cells, 5, rowIndex, "授课教师"),
                    getCellText(cells, 6, rowIndex, "考试时间"),
                    getCellText(cells, 7, rowIndex, "考场"),
                    getCellText(cells, 8, rowIndex, "座位号"),
                    getCellText(cells, 9, rowIndex, "准考证号"),
                    getCellText(cells, 10, rowIndex, "备注"),
                    getCellText(cells, 11, rowIndex, "操作")
            ));
        }
        return schedules;
    }

    private boolean isNoDataRow(Elements cells) {
        return cells.size() == 1 && cells.get(0).text().contains("未查询到数据");
    }

    private String getCellText(Elements cells, int index, int rowIndex, String fieldName) {
        if (index >= cells.size()) {
            throw new ParsingErrorException(
                    "解析考试安排时字段缺失: 第 " + (rowIndex + 1) + " 行, 字段[" + fieldName + "], 列索引=" + index
            );
        }
        return cells.get(index).text().trim();
    }
}
