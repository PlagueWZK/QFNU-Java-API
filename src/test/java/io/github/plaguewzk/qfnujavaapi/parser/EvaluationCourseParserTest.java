package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationCourse;
import io.github.plaguewzk.qfnujavaapi.parser.impl.EvaluationCourseParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationCourseParserTest {

    @Test
    void shouldParseSingleCourseRow() {
        EvaluationCourseParser parser = new EvaluationCourseParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th><th>课程编号</th><th>课程名称</th><th>授课教师</th>
                        <th>评教类别</th><th>总评分</th><th>已评</th><th>是否提交</th>
                        <th>讲课学时</th><th>实践学时</th><th>讲座学时</th>
                        <th>实验学时</th><th>设计学时</th><th>其中上机学时</th>
                        <th>讨论辅导学时</th><th>课外学时</th><th>网络学时</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>301409</td>
                        <td>软件体系结构与设计</td>
                        <td>刘双</td>
                        <td>评教课程</td>
                        <td>0</td>
                        <td>否</td>
                        <td>否</td>
                        <td>36</td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_edit.do?xnxq01id=2025-2026-2&pj01id=0C6E4478243641DEB09512383F76A80C&pj0502id=33E941C11E3E4AD2B52C0691D05E2C10&jx02id=1B2886593A6B4C3EA5CEA3819B881BC8&jx0404id=202520262000765&xsflid=1&jg0101id=20172820&zpf=0">评价</a>
                        </td>
                    </tr>
                </table>
                """;

        List<EvaluationCourse> courses = parser.parser(html);

        assertEquals(1, courses.size());
        EvaluationCourse c = courses.get(0);
        assertEquals("1", c.index());
        assertEquals("301409", c.courseId());
        assertEquals("软件体系结构与设计", c.courseName());
        assertEquals("刘双", c.teacher());
        assertEquals("评教课程", c.evalCategory());
        assertEquals("0", c.totalScore());
        assertEquals("否", c.evaluated());
        assertEquals("否", c.submitted());
        assertEquals("36", c.lectureHours());
        assertEquals("", c.practiceHours());
        assertEquals("", c.seminarHours());
        assertEquals("1B2886593A6B4C3EA5CEA3819B881BC8", c.jx02id());
        assertEquals("202520262000765", c.jx0404id());
        assertEquals("1", c.xsflid());
        assertEquals("20172820", c.jg0101id());
        assertEquals("0", c.zpf());
        assertTrue(c.evalUrl().contains("xspj_edit.do"));
    }

    @Test
    void shouldParseMultipleCourseRows() {
        EvaluationCourseParser parser = new EvaluationCourseParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th><th>课程编号</th><th>课程名称</th><th>授课教师</th>
                        <th>评教类别</th><th>总评分</th><th>已评</th><th>是否提交</th>
                        <th>讲课学时</th><th>实践学时</th><th>讲座学时</th>
                        <th>实验学时</th><th>设计学时</th><th>其中上机学时</th>
                        <th>讨论辅导学时</th><th>课外学时</th><th>网络学时</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>301409</td>
                        <td>软件体系结构</td>
                        <td>刘双</td>
                        <td>评教课程</td>
                        <td>0</td>
                        <td>否</td>
                        <td>否</td>
                        <td>36</td><td></td><td></td><td></td><td></td>
                        <td></td><td></td><td></td><td></td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_edit.do?jx02id=AAA&jx0404id=BBB&xsflid=1&jg0101id=CCC&zpf=0">评价</a>
                        </td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>302021</td>
                        <td>Linux程序设计</td>
                        <td>曹震中</td>
                        <td>评教课程</td>
                        <td>0</td>
                        <td>否</td>
                        <td>否</td>
                        <td>18</td><td></td><td></td><td>18</td><td></td>
                        <td></td><td></td><td></td><td></td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_edit.do?jx02id=DDD&jx0404id=EEE&xsflid=4&jg0101id=FFF&zpf=0">评价</a>
                        </td>
                    </tr>
                </table>
                """;

        List<EvaluationCourse> courses = parser.parser(html);

        assertEquals(2, courses.size());
        assertEquals("AAA", courses.get(0).jx02id());
        assertEquals("DDD", courses.get(1).jx02id());
        assertEquals("1", courses.get(0).xsflid());
        assertEquals("4", courses.get(1).xsflid());
        assertEquals("18", courses.get(1).experimentHours());
    }

    @Test
    void shouldParseCourseWithMixedCreditHours() {
        // 有的课时在实验学时列有值，有的在计算机学时/设计学时
        EvaluationCourseParser parser = new EvaluationCourseParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th><th>课程编号</th><th>课程名称</th><th>授课教师</th>
                        <th>评教类别</th><th>总评分</th><th>已评</th><th>是否提交</th>
                        <th>讲课学时</th><th>实践学时</th><th>讲座学时</th>
                        <th>实验学时</th><th>设计学时</th><th>其中上机学时</th>
                        <th>讨论辅导学时</th><th>课外学时</th><th>网络学时</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>302753</td>
                        <td>性能测试及工具</td>
                        <td>王妍</td>
                        <td>评教课程</td>
                        <td>0</td>
                        <td>否</td>
                        <td>否</td>
                        <td></td><td></td><td></td><td></td><td></td>
                        <td>36</td><td></td><td></td><td></td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_edit.do?jx02id=XXX&jx0404id=YYY&xsflid=6&jg0101id=ZZZ&zpf=0">评价</a>
                        </td>
                    </tr>
                </table>
                """;

        List<EvaluationCourse> courses = parser.parser(html);

        assertEquals(1, courses.size());
        assertEquals("", courses.get(0).lectureHours());
        assertEquals("36", courses.get(0).computerHours()); // 索引13: 其中上机学时
        assertEquals("6", courses.get(0).xsflid());
    }

    @Test
    void shouldThrowWhenDataTableMissing() {
        EvaluationCourseParser parser = new EvaluationCourseParser();
        assertThrows(PageStructureException.class, () ->
                parser.parser("<html><body>empty</body></html>"));
    }

    @Test
    void shouldThrowWhenHtmlIsBlank() {
        EvaluationCourseParser parser = new EvaluationCourseParser();
        assertThrows(PageStructureException.class, () -> parser.parser(""));
    }

    @Test
    void shouldReturnEmptyListWhenNoData() {
        EvaluationCourseParser parser = new EvaluationCourseParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th>
                    </tr>
                    <tr>
                        <td colspan='18'>未查询到数据</td>
                    </tr>
                </table>
                """;

        assertEquals(List.of(), parser.parser(html));
    }
}
