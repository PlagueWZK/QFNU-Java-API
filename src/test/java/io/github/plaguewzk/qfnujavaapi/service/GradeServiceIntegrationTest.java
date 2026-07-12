package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.BaseIntegrationTest;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GradeService 集成测试，使用真实教务系统数据。
 *
 * @author PlagueWZK
 */
@DisplayName("成绩服务集成测试")
class GradeServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("获取全部学期成绩报告")
    void shouldGetGradeReport() {
        GradeService service = client.service(GradeService.class);
        GradeReport report = service.getGradeReport();

        assertNotNull(report, "成绩报告不应为 null");
        assertNotNull(report.queryCondition(), "查询条件不应为 null");
        assertNotNull(report.grades(), "成绩列表不应为 null");
        assertTrue(report.totalCourseCount() >= 0, "课程总数应 >= 0");
    }

    @Test
    @DisplayName("获取全部学期成绩列表")
    void shouldGetGradeList() {
        GradeService service = client.service(GradeService.class);
        var grades = service.getGradeList();

        assertNotNull(grades, "成绩列表不应为 null");
        assertFalse(grades.isEmpty(), "应有至少一门课程成绩");
        // 验证第一条成绩的关键字段
        var first = grades.get(0);
        assertNotNull(first.courseName(), "课程名称不应为 null");
        assertNotNull(first.grade(), "成绩分数不应为 null");
    }
}
