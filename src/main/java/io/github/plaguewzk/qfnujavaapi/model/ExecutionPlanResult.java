package io.github.plaguewzk.qfnujavaapi.model;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

public record ExecutionPlanResult(int num, Term startTerm, String id, String courseName) {
}