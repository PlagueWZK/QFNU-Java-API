package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.model.grade.GradeReport;
import io.github.plaguewzk.qfnujavaapi.parser.impl.GradeReportParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeReportParserTest {

    @Test
    void shouldParseSummaryFromHtmlWhenPresent() {
        GradeReportParser parser = new GradeReportParser();
        String html = """
                <div>
                    查询条件：全部 所修门数:62 所修总学分:134 平均学分绩点:3.26 平均成绩:84.65
                    <table id='dataList'>
                        <tr><th>序号</th></tr>
                        <tr>
                            <td>1</td>
                            <td>2023-2024-1</td>
                            <td>301201</td>
                            <td>高等数学1</td>
                            <td></td>
                            <td>90</td>
                            <td></td>
                            <td>5</td>
                            <td>90</td>
                            <td>4</td>
                            <td></td>
                            <td>考试</td>
                            <td>正常考试</td>
                            <td>必修</td>
                            <td>专业必修课</td>
                            <td></td>
                        </tr>
                    </table>
                </div>
                """;

        GradeReport report = parser.parser(html);

        assertEquals("全部", report.queryCondition());
        assertEquals(62, report.totalCourseCount());
        assertEquals(134.0, report.totalCredits());
        assertEquals(3.26, report.averageCreditGradePoint());
        assertEquals(84.65, report.averageScore());
    }

    @Test
    void shouldCalculateSummaryWhenHtmlDoesNotContainAggregates() {
        GradeReportParser parser = new GradeReportParser();
        String html = """
                <div>
                    查询条件：开课时间【2024-2025-2】
                    <table id='dataList'>
                        <tr><th>序号</th></tr>
                        <tr>
                            <td>1</td>
                            <td>2024-2025-2</td>
                            <td>302020</td>
                            <td>操作系统</td>
                            <td></td>
                            <td>90</td>
                            <td></td>
                            <td>2</td>
                            <td>36</td>
                            <td>4</td>
                            <td></td>
                            <td>考试</td>
                            <td>正常考试</td>
                            <td>必修</td>
                            <td>专业必修课</td>
                            <td></td>
                        </tr>
                        <tr>
                            <td>2</td>
                            <td>2024-2025-2</td>
                            <td>302048</td>
                            <td>科技论文写作与前沿动态</td>
                            <td></td>
                            <td>80</td>
                            <td></td>
                            <td>1</td>
                            <td>18</td>
                            <td>1</td>
                            <td></td>
                            <td>考查</td>
                            <td>正常考试</td>
                            <td>任选</td>
                            <td>专业选修课</td>
                            <td></td>
                        </tr>
                    </table>
                </div>
                """;

        GradeReport report = parser.parser(html);

        assertEquals("开课时间【2024-2025-2】", report.queryCondition());
        assertEquals(2, report.totalCourseCount());
        assertEquals(3.0, report.totalCredits());
        assertEquals(3.0, report.averageCreditGradePoint());
        assertEquals(86.67, report.averageScore());
    }
}
