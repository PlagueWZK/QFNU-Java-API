package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评教表单页面解析后的完整数据，包含课程信息、表单隐藏字段和所有评价指标。
 *
 * @param courseName   课程名称
 * @param evalCategory 评教大类
 * @param totalScore   总评分
 * @param formFields   表单隐藏字段（pj09id, pj01id, pj0502id, jg0101id, jx0404id, xsflid, xnxq01id, jx02id, pj02id 等）
 * @param indicators   所有评价指标
 */
public record EvaluationFormData(
        String courseName,
        String evalCategory,
        String totalScore,
        Map<String, String> formFields,
        List<EvaluationIndicator> indicators
) {
    public EvaluationFormData {
        Objects.requireNonNull(courseName, "courseName");
        Objects.requireNonNull(evalCategory, "evalCategory");
        Objects.requireNonNull(totalScore, "totalScore");
        Objects.requireNonNull(formFields, "formFields");
        Objects.requireNonNull(indicators, "indicators");
    }
}
