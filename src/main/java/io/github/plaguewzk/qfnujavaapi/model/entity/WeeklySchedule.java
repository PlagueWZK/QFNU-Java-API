package io.github.plaguewzk.qfnujavaapi.model.entity;

import java.util.List;

/**
 * Created on 2026/1/2 22:28
 *
 * @author PlagueWZK
 */

public record WeeklySchedule(Integer currentWeek, Term term, List<CourseInfo> courseList) {
    public WeeklySchedule(Integer currentWeek, List<CourseInfo> courseList) {
        this(currentWeek, Term.current(), courseList);
    }
}
