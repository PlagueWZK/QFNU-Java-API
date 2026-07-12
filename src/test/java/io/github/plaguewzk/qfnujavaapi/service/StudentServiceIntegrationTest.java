package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.BaseIntegrationTest;
import io.github.plaguewzk.qfnujavaapi.TestConfig;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StudentService 集成测试，使用真实教务系统数据。
 * 仅测试非评教相关方法。
 *
 * @author PlagueWZK
 */
@DisplayName("学生信息服务集成测试")
class StudentServiceIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("获取当前登录学生基本信息")
    void shouldGetStudentInfo() {
        StudentService service = client.service(StudentService.class);
        StudentInfo info = service.getStudentInfo();

        assertNotNull(info, "学生信息不应为 null");
        assertFalse(info.name().isBlank(), "学生姓名不应为空");
        assertEquals(TestConfig.getAccount(), info.studentId(), "学号应与配置中的账号一致");
    }
}
