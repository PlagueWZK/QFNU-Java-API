package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.BaseIntegrationTest;
import io.github.plaguewzk.qfnujavaapi.model.course.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CourseService 集成测试，使用真实教务系统数据。
 *
 * @author PlagueWZK
 */
@DisplayName("课表服务集成测试")
@Slf4j
class CourseServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("获取当前学期课表")
    void shouldGetCurrentCourseTable() {
        CourseService service = client.service(CourseService.class);
        CourseTable table = service.getCurrentCourseTable();

        assertNotNull(table, "课表不应为 null");
        assertNotNull(table.term(), "学期不应为 null");
        assertNotNull(table.courses(), "课程列表不应为 null");
    }

    @Test
    @DisplayName("根据学期和周次获取课表")
    void shouldGetCourseTableByTermAndWeek() {
        CourseService service = client.service(CourseService.class);
        CourseTable current = service.getCurrentCourseTable();

        CourseTable table = service.getCourseTable(current.term(), current.week());

        assertNotNull(table, "课表不应为 null");
        assertEquals(current.term(), table.term(), "学期应一致");
        assertEquals(current.week(), table.week(), "周次应一致");
        assertNotNull(table.courses(), "课程列表不应为 null");
    }

    @Test
    void customTest(){
        CourseService service = client.service(CourseService.class);
        assertDoesNotThrow(() -> service.getCourseTable(Term.parse("2023-2024-1"), 8));
    }
}
