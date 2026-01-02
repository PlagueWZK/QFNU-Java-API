package io.github.plaguewzk.qfnujavaapi.model.entity;

public record CourseInfo(String courseName,
                         String credits,
                         String property,
                         String className,
                         String location,
                         String rawTime,
                         Integer dayOfWeek,
                         Integer startNode,
                         Integer endNode,
                         Integer week) {

}
