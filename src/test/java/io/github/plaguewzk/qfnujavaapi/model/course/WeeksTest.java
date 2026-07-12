package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Weeks 值对象单元测试。
 *
 * @author PlagueWZK
 */
@DisplayName("教学周次")
class WeeksTest {

    @Nested
    @DisplayName("构造与解析")
    class Construction {

        @Test
        @DisplayName("可变参数构造正常周次")
        void shouldConstructFromVarargs() {
            Weeks weeks = new Weeks(1, 3, 5);

            assertEquals(List.of(1, 3, 5), weeks.weeks());
        }

        @Test
        @DisplayName("列表构造自动排序去重")
        void shouldSortAndCopyOnConstruction() {
            Weeks weeks = new Weeks(List.of(5, 1, 3));

            assertEquals(List.of(1, 3, 5), weeks.weeks());
        }

        @Test
        @DisplayName("从字符串解析周次范围")
        void shouldParseWeekString() {
            Weeks weeks = Weeks.parse("1-18");

            assertEquals(18, weeks.weeks().size());
            assertEquals(1, weeks.weeks().get(0));
            assertEquals(18, weeks.weeks().get(17));
        }

        @Test
        @DisplayName("返回的列表为不可变副本")
        void shouldReturnUnmodifiableCopy() {
            Weeks weeks = new Weeks(1, 2, 3);
            List<Integer> list = weeks.weeks();

            assertThrows(UnsupportedOperationException.class, () -> list.add(4));
        }
    }

    @Nested
    @DisplayName("异常情况")
    class ErrorCases {

        @Test
        @DisplayName("周次为 null 时抛出 InvalidParameterException")
        void shouldRejectNullWeeks() {
            assertThrows(InvalidParameterException.class, () -> new Weeks((List<Integer>) null));
        }

        @Test
        @DisplayName("周次范围无效时抛出 InvalidParameterException")
        void shouldRejectInvalidWeekRange() {
            assertThrows(InvalidParameterException.class, () -> new Weeks(List.of(0)));
        }

        @Test
        @DisplayName("解析格式错误的字符串时抛出 InvalidParameterException")
        void shouldRejectMalformedWeekString() {
            assertThrows(InvalidParameterException.class, () -> Weeks.parse("1-a"));
        }
    }
}
