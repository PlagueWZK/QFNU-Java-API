package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QFNUClient Builder 单元测试。
 *
 * @author PlagueWZK
 */
@DisplayName("QFNUClient 构建器")
class QFNUClientTest {

    @Nested
    @DisplayName("参数校验")
    class ParameterValidation {

        @Test
        @DisplayName("账号为 null 时抛出 InvalidParameterException")
        void shouldRejectNullAccount() {
            QFNUClient.Builder builder = new QFNUClient.Builder();
            assertThrows(InvalidParameterException.class, () -> builder.account(null, "password"));
        }

        @Test
        @DisplayName("账号为空白字符串时抛出 InvalidParameterException")
        void shouldRejectBlankAccount() {
            QFNUClient.Builder builder = new QFNUClient.Builder();
            assertThrows(InvalidParameterException.class, () -> builder.account("  ", "password"));
        }

        @Test
        @DisplayName("密码为 null 时抛出 InvalidParameterException")
        void shouldRejectNullPassword() {
            QFNUClient.Builder builder = new QFNUClient.Builder();
            assertThrows(InvalidParameterException.class, () -> builder.account("student", null));
        }

        @Test
        @DisplayName("密码为空白字符串时抛出 InvalidParameterException")
        void shouldRejectBlankPassword() {
            QFNUClient.Builder builder = new QFNUClient.Builder();
            assertThrows(InvalidParameterException.class, () -> builder.account("student", "  "));
        }

        @Test
        @DisplayName("扩展模块为 null 时抛出 InvalidParameterException")
        void shouldRejectNullModule() {
            QFNUClient.Builder builder = new QFNUClient.Builder();
            assertThrows(InvalidParameterException.class, () -> builder.install(null));
        }
    }
}
