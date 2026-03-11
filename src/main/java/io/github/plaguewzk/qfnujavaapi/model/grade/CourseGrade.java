package io.github.plaguewzk.qfnujavaapi.model.grade;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

public record CourseGrade(
        Integer serialNumber, Term startSemester, String courseId, String courseName,
        String groupName,
        String grade, String gradeSymbol, Double credit, Integer classHours,
        Double gradePointAverage, Term makeUpSemester, AssessmentMethod assessmentMethod,
        String examinationNature, CourseAttributes courseAttributes, CourseNature courseNature,
        String courseCategories) {
}
