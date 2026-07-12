package io.github.plaguewzk.qfnujavaapi.model.evaluation;

/**
 * 评教五个评级选项。
 *
 * @author PlagueWZK
 */
public enum EvaluationRating {
    EXCELLENT("优秀"),
    GOOD("良好"),
    MEDIUM("中等"),
    PASS("及格"),
    FAIL("不及格");

    public final String label;

    EvaluationRating(String label) {
        this.label = label;
    }

    /**
     * 根据标签前缀匹配评级。如 "优秀(10)" 匹配 EXCELLENT。
     */
    public static EvaluationRating fromLabel(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        for (EvaluationRating r : values()) {
            if (label.startsWith(r.label)) {
                return r;
            }
        }
        return null;
    }
}
