package io.github.plaguewzk.qfnujavaapi.model.grade;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

public record CourseGrade(
        Integer serialNumber, Term startSemester, String courseNumber, String courseName,
        String groupName,
        String grade, String gradeSymbol, Integer credit, Integer classHours,
        Integer gradePointAverage, Term makeUpSemester, AssessmentMethod assessmentMethod,
        String natureOfTheExamination, CourseAttributes courseAttributes, String courseNature,
        String courseCategories) {
}
