package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 自动评教评分算法工具类。
 *
 * <h3>算法说明</h3>
 *
 * <h4>方案一：最接近满分</h4>
 * 全部指标默认选"优秀"。然后找一个扣分最少的指标降为"良好"，使总分最接近满分。
 * 具体做法：计算每个指标的 优秀分值 - 良好分值，取差值最小的那个指标转为"良好"。
 * 如果多个指标的差值相等（可能有多个解），取第一个。
 *
 * <h4>方案二：最接近 90 分</h4>
 * 满分为 100，目标为 90 分以下最接近的值。每个指标从"优秀"降为"良好"会扣掉
 * {@code 优秀分值 - 良好分值}。枚举所有 2^9=512 种组合，找到<b>扣分严格大于目标扣分</b>
 * （确保总分严格低于 90）且最接近的组合。如果不存在严格大于的组合，则退化为找最接近的组合。
 * 可能存在多个接近 90 的解，算法返回第一个最优解。
 *
 * @author PlagueWZK
 */
public final class EvaluationAutoScorer {

    /** 将分数乘以 SCALE 转为整数，避免浮点精度问题 */
    private static final int SCALE = 100;
    private static final int TARGET_90_SCALED = 9000;

    private EvaluationAutoScorer() {}

    /**
     * 计算最接近满分的评分方案（只有一个指标为"良好"，其余为"优秀"）。
     *
     * @param formData 评教表单数据
     * @return 指标序号 → 评级 的映射
     */
    public static Map<Integer, EvaluationRating> computeClosestToFull(EvaluationFormData formData) {
        Objects.requireNonNull(formData, "formData");
        List<EvaluationIndicator> indicators = formData.indicators();

        Map<Integer, EvaluationRating> result = new LinkedHashMap<>();

        // 找优秀与良好分差最小的指标
        int bestIndex = -1;
        int minDiff = Integer.MAX_VALUE;

        for (EvaluationIndicator indicator : indicators) {
            var excelOpt = indicator.getOption(EvaluationRating.EXCELLENT);
            var goodOpt = indicator.getOption(EvaluationRating.GOOD);
            if (excelOpt.isEmpty() || goodOpt.isEmpty()) continue;

            int diff = toScaledInt(excelOpt.get().score()) - toScaledInt(goodOpt.get().score());
            if (diff < minDiff) {
                minDiff = diff;
                bestIndex = indicator.index();
            }

            result.put(indicator.index(), EvaluationRating.EXCELLENT);
        }

        if (bestIndex > 0) {
            result.put(bestIndex, EvaluationRating.GOOD);
        }
        return result;
    }

    /**
     * 计算最接近 90 分的评分方案。
     *
     * @param formData 评教表单数据
     * @return 指标序号 → 评级 的映射
     */
    public static Map<Integer, EvaluationRating> computeClosestTo90(EvaluationFormData formData) {
        Objects.requireNonNull(formData, "formData");
        List<EvaluationIndicator> indicators = formData.indicators();

        // 1. 计算每个指标的差值 + 总满分
        List<IndicatorDiff> diffs = new ArrayList<>();
        int totalMaxScaled = 0;

        for (EvaluationIndicator indicator : indicators) {
            var excelOpt = indicator.getOption(EvaluationRating.EXCELLENT);
            var goodOpt = indicator.getOption(EvaluationRating.GOOD);
            if (excelOpt.isEmpty() || goodOpt.isEmpty()) continue;

            int excelScaled = toScaledInt(excelOpt.get().score());
            int goodScaled = toScaledInt(goodOpt.get().score());
            totalMaxScaled += excelScaled;

            diffs.add(new IndicatorDiff(indicator.index(), excelScaled - goodScaled));
        }

        // 目标扣分 = 满分 - 90
        int targetLossScaled = totalMaxScaled - TARGET_90_SCALED;

        // 2. 枚举所有子集，找"扣分严格大于目标"且最接近的组合（确保总分 < 90）
        int n = diffs.size();
        int bestMask = 0;
        long bestDist = Long.MAX_VALUE;      // 严格大于目标的距离
        long bestDistFallback = Long.MAX_VALUE; // 兜底：不限方向的最近距离
        int bestMaskFallback = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            int loss = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    loss += diffs.get(i).diff;
                }
            }

            // 只能接受 loss > targetLossScaled（总分严格 < 90）
            if (loss > targetLossScaled) {
                long dist = (long) loss - targetLossScaled;
                if (dist < bestDist) {
                    bestDist = dist;
                    bestMask = mask;
                }
            }

            // 记录兜底方案（以防没有严格大于的组合）
            long dist = Math.abs((long) loss - targetLossScaled);
            if (dist < bestDistFallback) {
                bestDistFallback = dist;
                bestMaskFallback = mask;
            }
        }

        // 如果没有严格大于目标的组合，使用兜底方案
        if (bestDist == Long.MAX_VALUE) {
            bestMask = bestMaskFallback;
        }

        // 3. 构建结果
        Map<Integer, EvaluationRating> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            int index = diffs.get(i).index;
            if ((bestMask & (1 << i)) != 0) {
                result.put(index, EvaluationRating.GOOD);
            } else {
                result.put(index, EvaluationRating.EXCELLENT);
            }
        }
        return result;
    }

    /**
     * 计算指定方案的预期总分。
     */
    public static double computeTotalScore(EvaluationFormData formData, Map<Integer, EvaluationRating> scheme) {
        double total = 0;
        for (EvaluationIndicator indicator : formData.indicators()) {
            EvaluationRating rating = scheme.get(indicator.index());
            if (rating == null) continue;
            var opt = indicator.getOption(rating);
            if (opt.isPresent()) {
                total += Double.parseDouble(opt.get().score());
            }
        }
        return total;
    }

    private static int toScaledInt(String scoreStr) {
        return (int) Math.round(Double.parseDouble(scoreStr) * SCALE);
    }

    private record IndicatorDiff(int index, int diff) {}
}
