package io.github.plaguewzk.qfnujavaapi.model.entity;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CourseExceptionTest {

    @Test
    void shouldRejectInvalidWeekRange() {
        assertThrows(InvalidParameterException.class, () -> new Course.Weeks(List.of(0)));
    }

    @Test
    void shouldRejectMalformedWeekString() {
        assertThrows(InvalidParameterException.class, () -> Course.Weeks.parse("1-a"));
    }
}
