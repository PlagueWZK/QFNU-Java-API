package io.github.plaguewzk.qfnujavaapi.model.course;

import java.util.List;

public record WeeklySchedule(Integer currentWeek, Term term, List<CourseInfo> courseList) {
    public WeeklySchedule(Integer currentWeek, List<CourseInfo> courseList) {
        this(currentWeek, Term.current(), courseList);
    }
}
