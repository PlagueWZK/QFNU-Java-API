package io.github.plaguewzk.qfnujavaapi.model.course;

public record Course(Weekday weekday,
                     String courseName,
                     Weeks weeks,
                     Section section,
                     String location,
                     String teacher) {
}
