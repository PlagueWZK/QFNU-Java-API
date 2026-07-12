package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.BaseIntegrationTest;
import io.github.plaguewzk.qfnujavaapi.model.exam.ExamSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExamScheduleService 集成测试，使用真实教务系统数据。
 *
 * @author PlagueWZK
 */
@DisplayName("考试安排服务集成测试")
class ExamScheduleServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("获取考试安排列表")
    void shouldGetExamSchedules() {
        ExamScheduleService service = client.service(ExamScheduleService.class);
        List<ExamSchedule> schedules = service.getExamSchedules();

        assertNotNull(schedules, "考试安排列表不应为 null");
        // 可能为空（当前无考试安排），不强制要求非空
    }
}
