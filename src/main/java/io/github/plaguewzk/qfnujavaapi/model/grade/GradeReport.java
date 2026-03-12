package io.github.plaguewzk.qfnujavaapi.model.grade;

import java.util.List;

public record GradeReport(
        String queryCondition,
        Integer totalCourseCount,
        Double totalCredits,
        Double averageCreditGradePoint,
        Double averageScore,
        List<CourseGrade> grades
) {
}
