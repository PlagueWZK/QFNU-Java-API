package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.course.Course;
import io.github.plaguewzk.qfnujavaapi.model.course.SectionConstant;
import io.github.plaguewzk.qfnujavaapi.model.course.Weekday;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CourseParser 单元测试。
 *
 * @author PlagueWZK
 */
@DisplayName("课程条目解析器")
class CourseParserTest {

    private final CourseParser parser = new CourseParser();

    @Nested
    @DisplayName("正常解析")
    class HappyPath {

        @Test
        @DisplayName("解析单节次课程")
        void shouldParseSingleSectionCourse() {
            String html = """
                    <div class='kbcontent'>
                        高等数学1<br/>
                        <font title='老师'>王老师</font><br/>
                        <font title='周次(节次)'>1-18(周)[01-02节]</font><br/>
                        <font title='教室'>A101</font>
                    </div>
                    """;

            List<Course> courses = parser.parser(html, Weekday.MONDAY);

            assertEquals(1, courses.size());
            Course c = courses.get(0);
            assertEquals(Weekday.MONDAY, c.weekday());
            assertEquals("高等数学1", c.courseName());
            assertEquals("王老师", c.teacher());
            assertEquals("A101", c.location());
            assertNotNull(c.weeks());
            assertNotNull(c.section());
            assertEquals(SectionConstant.S01, c.section().start());
            assertEquals(SectionConstant.S02, c.section().end());
        }

        @Test
        @DisplayName("解析三节次连排课程")
        void shouldParseTripleSectionCourse() {
            String html = """
                    <div class='kbcontent'>
                        软件体系结构与设计<br/>
                        <font title='老师'>刘老师</font><br/>
                        <font title='周次(节次)'>1-12(周)[01-02-03节]</font><br/>
                        <font title='教室'>格物楼B221</font>
                    </div>
                    """;

            List<Course> courses = parser.parser(html, Weekday.THURSDAY);

            assertEquals(1, courses.size());
            Course c = courses.get(0);
            assertEquals("软件体系结构与设计", c.courseName());
            assertEquals(SectionConstant.S01, c.section().start());
            assertEquals(SectionConstant.S03, c.section().end());
        }

        @Test
        @DisplayName("解析被分隔线分开的多门课程")
        void shouldParseMultipleCoursesSeparatedByDivider() {
            String html = """
                    <div class='kbcontent'>
                        Java程序设计<br/>
                        <font title='老师'>赵老师</font><br/>
                        <font title='周次(节次)'>1-8(周)[01-02节]</font><br/>
                        <font title='教室'>A201</font><br/>
                        ----<br/>
                        数据结构<br/>
                        <font title='老师'>钱老师</font><br/>
                        <font title='周次(节次)'>9-18(周)[01-02节]</font><br/>
                        <font title='教室'>A201</font>
                    </div>
                    """;

            List<Course> courses = parser.parser(html, Weekday.WEDNESDAY);

            assertEquals(2, courses.size());
            assertEquals("Java程序设计", courses.get(0).courseName());
            assertEquals("数据结构", courses.get(1).courseName());
        }

        @Test
        @DisplayName("空 kbcontent 或无 kbcontent 返回空列表")
        void shouldReturnEmptyListForEmptyOrMissingContent() {
            assertEquals(List.of(), parser.parser(""));
            assertEquals(List.of(), parser.parser("<div class='other'>content</div>"));
            assertEquals(List.of(), parser.parser("<div class='kbcontent'>&nbsp;</div>"));
        }
    }

    @Nested
    @DisplayName("异常情况")
    class ErrorCases {

        @Test
        @DisplayName("缺少周次信息时抛出 ParsingErrorException")
        void shouldThrowWhenTimeInfoMissing() {
            String html = """
                    <div class='kbcontent'>
                        Java程序设计<br/>
                        <font title='老师'>张三</font><br/>
                        <font title='教室'>A101</font>
                    </div>
                    """;

            assertThrows(ParsingErrorException.class, () -> parser.parser(html));
        }
    }
}
