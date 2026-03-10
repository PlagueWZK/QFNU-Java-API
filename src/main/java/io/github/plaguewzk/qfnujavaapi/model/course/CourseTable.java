package io.github.plaguewzk.qfnujavaapi.model.course;

import java.util.List;

public record CourseTable(Term term, Integer week, List<Course> courses, String note, String remark) {
}
