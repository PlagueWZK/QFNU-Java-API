package io.github.plaguewzk.qfnujavaapi.model.grade;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("成绩查询参数")
class GradeQueryTest {

    @Test
    @DisplayName("构建默认查询参数")
    void shouldBuildDefaultQueryParameters() {
        GradeQuery query = GradeQuery.defaultQuery();

        assertEquals(
                Map.of(
                        "kksj", "",
                        "kcxz", "",
                        "kcmc", "",
                        "xsfs", "all"
                ),
                query.toMap()
        );
    }

    @Test
    @DisplayName("构建自定义查询参数")
    void shouldBuildCustomQueryParameters() {
        GradeQuery query = GradeQuery.builder()
                .startSemester(Term.parse("2025-2026-1"))
                .courseNature(CourseNature.PUBLIC_REQUIRED_COURSES)
                .courseName("软件工程")
                .displayMode(GradeDisplayMode.ALL)
                .build();

        assertEquals("2025-2026-1", query.kksj());
        assertEquals("16", query.kcxz());
        assertEquals("软件工程", query.kcmc());
        assertEquals("all", query.xsfs());
    }
}
