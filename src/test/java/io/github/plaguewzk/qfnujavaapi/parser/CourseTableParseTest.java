package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.model.course.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.course.Weekday;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseTableParse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseTableParseTest {

    @Test
    void shouldParseWeekdayFromColumnIndex() {
        CourseTableParse parser = new CourseTableParse();
        String html = """
                <html>
                <body>
                <table id='kbtable'>
                    <tr>
                        <th>&nbsp;</th>
                        <th>星期一</th>
                        <th>星期二</th>
                        <th>星期三</th>
                        <th>星期四</th>
                        <th>星期五</th>
                        <th>星期六</th>
                        <th>星期日</th>
                    </tr>
                    <tr>
                        <th>第1~2节</th>
                        <td>&nbsp;</td>
                        <td>
                            <div class='kbcontent'>
                                性能测试及工具<br/>
                                <font title='老师'>王妍</font><br/>
                                <font title='周次(节次)'>10-18(周)[01-02节]</font><br/>
                                <font title='教室'>嵌入式实验室204</font><br/>
                            </div>
                        </td>
                        <td>&nbsp;</td>
                        <td>
                            <div class='kbcontent'>
                                软件体系结构与设计<br/>
                                <font title='老师'>刘双</font><br/>
                                <font title='周次(节次)'>1-12(周)[01-02-03节]</font><br/>
                                <font title='教室'>格物楼B221</font><br/>
                            </div>
                        </td>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                    </tr>
                </table>
                <select id='xnxq01id'><option selected='selected'>2025-2026-2</option></select>
                <select id='zc'><option value='1' selected='selected'>第1周</option></select>
                学期理论课表
                </body>
                </html>
                """;

        CourseTable courseTable = parser.parser(html);

        assertEquals(2, courseTable.courses().size());
        assertEquals(Weekday.TUESDAY, courseTable.courses().get(0).weekday());
        assertEquals(Weekday.THURSDAY, courseTable.courses().get(1).weekday());
    }
}
