package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Term 值对象单元测试。
 *
 * @author PlagueWZK
 */
@DisplayName("学期")
class TermTest {

    @Nested
    @DisplayName("构造")
    class Construction {

        @Test
        @DisplayName("三参数构造正常学期")
        void shouldConstructWithThreeParams() {
            Term term = new Term(2025, 2026, 2);

            assertEquals(2025, term.startYear());
            assertEquals(2026, term.endYear());
            assertEquals(2, term.termIndex());
        }

        @Test
        @DisplayName("双参数构造自动推断结束年份")
        void shouldInferEndYearWithTwoParams() {
            Term term = new Term(2025, 1);

            assertEquals(2025, term.startYear());
            assertEquals(2026, term.endYear());
            assertEquals(1, term.termIndex());
        }

        @Test
        @DisplayName("toString 返回标准学期格式")
        void shouldReturnStandardFormat() {
            Term term = new Term(2025, 2026, 2);

            assertEquals("2025-2026-2", term.toString());
        }
    }

    @Nested
    @DisplayName("解析")
    class Parsing {

        @Test
        @DisplayName("从标准字符串解析学期")
        void shouldParseStandardTermString() {
            Term term = Term.parse("2025-2026-1");

            assertEquals(2025, term.startYear());
            assertEquals(2026, term.endYear());
            assertEquals(1, term.termIndex());
        }
    }

    @Nested
    @DisplayName("异常情况")
    class ErrorCases {

        @Test
        @DisplayName("年份差不为 1 时抛出 InvalidParameterException")
        void shouldRejectInvalidYearRange() {
            assertThrows(InvalidParameterException.class, () -> new Term(2025, 2027, 1));
            assertThrows(InvalidParameterException.class, () -> new Term(2025, 2025, 1));
        }

        @Test
        @DisplayName("学期序号不在 [1,2] 范围时抛出 InvalidParameterException")
        void shouldRejectInvalidTermIndex() {
            assertThrows(InvalidParameterException.class, () -> new Term(2025, 2026, 0));
            assertThrows(InvalidParameterException.class, () -> new Term(2025, 2026, 3));
        }

        @Test
        @DisplayName("解析格式错误的字符串时抛出 InvalidParameterException")
        void shouldRejectMalformedTermString() {
            assertThrows(InvalidParameterException.class, () -> Term.parse("2025-2026"));
            assertThrows(InvalidParameterException.class, () -> Term.parse("abc"));
            assertThrows(InvalidParameterException.class, () -> Term.parse(""));
        }
    }
}
