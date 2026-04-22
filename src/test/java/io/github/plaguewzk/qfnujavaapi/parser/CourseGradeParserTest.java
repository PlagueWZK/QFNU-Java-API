package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseGradeParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CourseGradeParserTest {

    @Test
    void shouldParseGradeRowWithBlankOptionalFields() {
        CourseGradeParser parser = new CourseGradeParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th>
                    </tr>
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
                """;

        List<CourseGrade> grades = parser.parser(html);

        assertEquals(1, grades.size());
        assertNull(grades.get(0).groupName());
        assertNull(grades.get(0).gradeSymbol());
        assertNull(grades.get(0).makeUpSemester());
        assertNull(grades.get(0).courseCategories());
        assertEquals("高等数学1", grades.get(0).courseName());
        assertEquals(4.0, grades.get(0).gradePointAverage());
    }

    @Test
    void shouldSkipRowWhenOptionalFieldFormatIsInvalid() {
        CourseGradeParser parser = new CourseGradeParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th>
                    </tr>
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
                        <td>bad-term</td>
                        <td>考试</td>
                        <td>正常考试</td>
                        <td>必修</td>
                        <td>专业必修课</td>
                        <td></td>
                    </tr>
                </table>
                """;

        List<CourseGrade> grades = parser.parser(html);

        assertEquals(List.of(), grades);
    }
}
