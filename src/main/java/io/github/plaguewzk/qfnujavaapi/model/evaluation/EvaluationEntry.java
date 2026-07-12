package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

import java.util.Objects;

/**
 * 学生评价列表中的一条评价入口。
 *
 * @param index     序号
 * @param term      学年学期
 * @param category  评价分类（如 学生评教）
 * @param batch     评价批次（如 2025-2026-2学生评教）
 * @param startDate 开始时间
 * @param endDate   结束时间
 * @param pj0502id  评价批次ID（用于后续请求）
 * @param xnxq01id  学年学期ID
 * @param pj01id    评价分类ID（可能为空）
 * @param enterUrl  进入评价的完整相对路径
 */
public record EvaluationEntry(
        String index,
        Term term,
        String category,
        String batch,
        String startDate,
        String endDate,
        String pj0502id,
        String xnxq01id,
        String pj01id,
        String enterUrl
) {
    public EvaluationEntry {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(term, "term");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(batch, "batch");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        // 以下字段可能为 null 或空字符串
    }
}
