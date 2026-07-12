package io.github.plaguewzk.qfnujavaapi.model.evaluation;

/**
 * 评教指标枚举，将抽象的 pj06xh 索引映射为语义化名称。
 * 这些指标在不同课程中对应的序号（pj06xh）基本固定。
 *
 * @author PlagueWZK
 */
public enum EvaluationIndicatorType {
    /** 师德师风 */
    TEACHER_ETHICS(2),
    /** 教学准备 */
    TEACHING_PREPARATION(3),
    /** 内容质量 */
    CONTENT_QUALITY(7),
    /** 方法技能 */
    TEACHING_METHOD(4),
    /** 技术运用 */
    TECHNOLOGY_USE(5),
    /** 专业素养 */
    PROFESSIONAL_QUALITY(6),
    /** 学习体验 */
    LEARNING_EXPERIENCE(8),
    /** 考核反馈 */
    ASSESSMENT_FEEDBACK(9),
    /** 反思创新 */
    REFLECTION_INNOVATION(10);

    public final int index;

    EvaluationIndicatorType(int index) {
        this.index = index;
    }

    /**
     * 根据 pj06xh 索引获取对应的枚举实例。
     */
    public static EvaluationIndicatorType fromIndex(int index) {
        for (EvaluationIndicatorType type : values()) {
            if (type.index == index) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的评教指标序号: " + index);
    }
}
