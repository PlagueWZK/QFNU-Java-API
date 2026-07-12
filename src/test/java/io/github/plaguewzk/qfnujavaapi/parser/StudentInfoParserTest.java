package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.impl.StudentInfoParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StudentInfoParser 单元测试。
 *
 * @author PlagueWZK
 */
@DisplayName("学生信息解析器")
class StudentInfoParserTest {

    private final StudentInfoParser parser = new StudentInfoParser();

    @Nested
    @DisplayName("正常解析")
    class HappyPath {

        @Test
        @DisplayName("解析完整的学生信息")
        void shouldParseCompleteStudentInfo() {
            String html = """
                    <div class='middletopttxlr'>
                        <div>
                            <div class='middletopdwxxtit'>姓名</div>
                            <div class='middletopdwxxcont'>张三</div>
                        </div>
                        <div>
                            <div class='middletopdwxxtit'>编号</div>
                            <div class='middletopdwxxcont'>2023001</div>
                        </div>
                        <div>
                            <div class='middletopdwxxtit'>院系</div>
                            <div class='middletopdwxxcont'>计算机学院</div>
                        </div>
                        <div>
                            <div class='middletopdwxxtit'>专业</div>
                            <div class='middletopdwxxcont'>软件工程</div>
                        </div>
                        <div>
                            <div class='middletopdwxxtit'>班级</div>
                            <div class='middletopdwxxcont'>软件工程1班</div>
                        </div>
                    </div>
                    """;

            StudentInfo info = parser.parser(html);

            assertEquals("张三", info.name());
            assertEquals("2023001", info.studentId());
            assertEquals("计算机学院", info.academy());
            assertEquals("软件工程", info.major());
            assertEquals("软件工程1班", info.className());
        }

        @Test
        @DisplayName("院系/专业/班级字段缺失时不抛异常")
        void shouldAllowMissingOptionalFields() {
            String html = """
                    <div class='middletopttxlr'>
                        <div>
                            <div class='middletopdwxxtit'>姓名</div>
                            <div class='middletopdwxxcont'>李四</div>
                        </div>
                        <div>
                            <div class='middletopdwxxtit'>编号</div>
                            <div class='middletopdwxxcont'>2023002</div>
                        </div>
                    </div>
                    """;

            StudentInfo info = parser.parser(html);

            assertEquals("李四", info.name());
            assertEquals("2023002", info.studentId());
            assertNull(info.academy());
            assertNull(info.major());
            assertNull(info.className());
        }
    }

    @Nested
    @DisplayName("异常情况")
    class ErrorCases {

        @Test
        @DisplayName("缺少 .middletopttxlr 容器时抛出 PageStructureException")
        void shouldThrowWhenContainerMissing() {
            assertThrows(PageStructureException.class, () -> parser.parser("<html><body></body></html>"));
        }

        @Test
        @DisplayName("缺少姓名或学号时抛出 ParsingErrorException")
        void shouldThrowWhenEssentialFieldsMissing() {
            String html = """
                    <div class='middletopttxlr'>
                        <div>
                            <div class='middletopdwxxtit'>院系</div>
                            <div class='middletopdwxxcont'>计算机学院</div>
                        </div>
                    </div>
                    """;

            assertThrows(ParsingErrorException.class, () -> parser.parser(html));
        }
    }
}
