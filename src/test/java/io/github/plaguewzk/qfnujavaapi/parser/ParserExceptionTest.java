package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseInfoParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.StudentInfoParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserExceptionTest {

    @Test
    void shouldThrowSystemChangedWhenStudentInfoContainerMissing() {
        StudentInfoParser parser = new StudentInfoParser();

        assertThrows(PageStructureException.class, () -> parser.parser("<html><body></body></html>"));
    }

    @Test
    void shouldThrowParsingErrorWhenStudentInfoEssentialFieldsMissing() {
        StudentInfoParser parser = new StudentInfoParser();
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

    @Test
    void shouldThrowParsingErrorWhenCourseTimeMissing() {
        CourseParser parser = new CourseParser();
        String html = """
                <div class='kbcontent'>
                    Java程序设计<br/>
                    <font title='老师'>张三</font><br/>
                    <font title='教室'>A101</font>
                </div>
                """;

        assertThrows(ParsingErrorException.class, () -> parser.parser(html));
    }

    @Test
    void shouldThrowParsingErrorWhenCourseInfoTitleMissing() {
        CourseInfoParser parser = new CourseInfoParser();

        assertThrows(ParsingErrorException.class, () -> parser.parser("<p>empty</p>"));
    }
}
