package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.exam.ExamSchedule;
import io.github.plaguewzk.qfnujavaapi.parser.impl.ExamScheduleParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("考试安排解析器")
class ExamScheduleParserTest {

    @Test
    @DisplayName("解析考试安排行数据")
    void shouldParseExamScheduleRow() {
        ExamScheduleParser parser = new ExamScheduleParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>曲阜校区</td>
                        <td>第1场</td>
                        <td>CS101</td>
                        <td>数据结构</td>
                        <td>张三</td>
                        <td>2026-06-20 09:00-11:00</td>
                        <td>A101</td>
                        <td>08</td>
                        <td>ZK2026001</td>
                        <td>闭卷</td>
                        <td>查看</td>
                    </tr>
                </table>
                """;

        List<ExamSchedule> schedules = parser.parser(html);

        assertEquals(1, schedules.size());
        assertEquals("1", schedules.get(0).index());
        assertEquals("曲阜校区", schedules.get(0).campus());
        assertEquals("数据结构", schedules.get(0).courseName());
        assertEquals("2026-06-20 09:00-11:00", schedules.get(0).examTime());
        assertEquals("ZK2026001", schedules.get(0).admissionNo());
    }

    @Test
    @DisplayName("无数据行时返回空列表")
    void shouldReturnEmptyListWhenRowSaysNoData() {
        ExamScheduleParser parser = new ExamScheduleParser();
        String html = """
                <table id='dataList'>
                    <tr>
                        <th>序号</th>
                    </tr>
                    <tr>
                        <td colspan='12'>未查询到数据</td>
                    </tr>
                </table>
                """;

        assertEquals(List.of(), parser.parser(html));
    }

    @Test
    @DisplayName("数据表格缺失时抛出 PageStructureException")
    void shouldThrowWhenDataTableMissing() {
        ExamScheduleParser parser = new ExamScheduleParser();

        assertThrows(PageStructureException.class, () -> parser.parser("<html><body>empty</body></html>"));
    }
}
