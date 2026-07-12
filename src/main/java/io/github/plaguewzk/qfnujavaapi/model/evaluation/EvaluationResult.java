package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.Objects;

/**
 * 自动评教结果。
 *
 * @param course       评教课程
 * @param scheme       使用的评分方案
 * @param success      是否成功
 * @param score        预估总分（失败时为 0）
 * @param errorMessage 失败时的错误摘要（成功时为 null）
 */
public record EvaluationResult(
        EvaluationCourse course,
        EvaluationScheme scheme,
        boolean success,
        double score,
        String errorMessage
) {
    public EvaluationResult {
        Objects.requireNonNull(course, "course");
        Objects.requireNonNull(scheme, "scheme");
    }

    public static EvaluationResult success(EvaluationCourse course, EvaluationScheme scheme, double score) {
        return new EvaluationResult(course, scheme, true, score, null);
    }

    public static EvaluationResult failure(EvaluationCourse course, EvaluationScheme scheme, String errorMessage) {
        return new EvaluationResult(course, scheme, false, 0.0, errorMessage);
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("[✓] %s (%s) → %.2f 分", course.courseName(), scheme, score);
        }
        return String.format("[✗] %s (%s) → %s", course.courseName(), scheme, errorMessage);
    }
}
