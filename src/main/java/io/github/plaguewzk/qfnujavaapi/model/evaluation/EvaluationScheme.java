package io.github.plaguewzk.qfnujavaapi.model.evaluation;

/**
 * 自动评教评分方案。
 *
 * @author PlagueWZK
 */
public enum EvaluationScheme {
    /** 最接近满分（约 98.02），仅一个指标降为"良好" */
    CLOSEST_TO_FULL,
    /** 最接近 90 分（约 89.98），在 90 分以下最接近 */
    CLOSEST_TO_90
}
