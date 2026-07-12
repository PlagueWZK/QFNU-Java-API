package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 评教表单中的一个评价指标。
 *
 * @param index       指标序号（pj06xh 值）
 * @param category    所属大类（如 "教学素养"）
 * @param description 指标描述文本
 * @param options     该指标的全部五个评级选项（按 优秀→不及格 顺序排列）
 */
public record EvaluationIndicator(
        int index,
        String category,
        String description,
        List<EvaluationIndicatorOption> options
) {
    public EvaluationIndicator {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(options, "options");
    }

    /**
     * 获取指定评级的选项。
     */
    public Optional<EvaluationIndicatorOption> getOption(EvaluationRating rating) {
        return options.stream()
                .filter(o -> o.rating() == rating)
                .findFirst();
    }
}
