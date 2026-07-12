package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.Objects;

/**
 * 评教指标的一个选项。
 *
 * @param optionId 选项ID（pj0601id 值）
 * @param label    选项标签（如 "优秀(10)"）
 * @param score    选项分值（如 "10" 或 "10.2"）
 * @param rating   对应的评级枚举
 */
public record EvaluationIndicatorOption(
        String optionId,
        String label,
        String score,
        EvaluationRating rating
) {
    public EvaluationIndicatorOption {
        Objects.requireNonNull(optionId, "optionId");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(score, "score");
        Objects.requireNonNull(rating, "rating");
    }
}
