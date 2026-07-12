package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import io.github.plaguewzk.qfnujavaapi.util.Util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评教提交参数，使用 Builder 模式构建。
 *
 * <h3>用法示例</h3>
 * <pre>{@code
 * // 一键评教：所有指标选"优秀"并保存
 * EvaluationSubmission submission = EvaluationSubmission.builder()
 *         .fromForm(formData)
 *         .indicatorAll(EvaluationRating.EXCELLENT)
 *         .build();
 *
 * // 自定义评教：不同指标选不同评级
 * EvaluationSubmission submission = EvaluationSubmission.builder()
 *         .fromForm(formData)
 *         .indicator(2, EvaluationRating.EXCELLENT)
 *         .indicator(3, EvaluationRating.GOOD)
 *         .indicatorAll(EvaluationRating.MEDIUM)
 *         .build();
 * }</pre>
 *
 * @author PlagueWZK
 */
@SuppressWarnings("SpellCheckingInspection")
public final class EvaluationSubmission {
    private final Map<String, String> formFields;
    private final List<EvaluationIndicator> indicators;
    private final Map<Integer, String> indicatorSelections; // index → optionId
    private final int isxtjg;

    private EvaluationSubmission(Builder builder) {
        this.formFields = new LinkedHashMap<>(builder.formFields);
        this.indicators = List.copyOf(builder.indicators);
        this.indicatorSelections = new LinkedHashMap<>(builder.indicatorSelections);
        this.isxtjg = builder.isxtjg;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建 URL 编码的 POST 表单体字符串，支持重复 key。
     */
    public String toQueryString() {
        StringBuilder sb = new StringBuilder();

        // 1. 表单固定字段（issubmit 始终为 0，仅保存不提交；最终提交走独立 API）
        appendParam(sb, "issubmit", "0");
        appendParam(sb, "sfxyt", formFields.getOrDefault("sfxyt", "0"));
        appendParam(sb, "pj09id", formFields.getOrDefault("pj09id", ""));
        appendParam(sb, "pj01id", formFields.getOrDefault("pj01id", ""));
        appendParam(sb, "pj0502id", formFields.getOrDefault("pj0502id", ""));
        appendParam(sb, "jg0101id", formFields.getOrDefault("jg0101id", ""));
        appendParam(sb, "jx0404id", formFields.getOrDefault("jx0404id", ""));
        appendParam(sb, "xsflid", formFields.getOrDefault("xsflid", ""));
        appendParam(sb, "xnxq01id", formFields.getOrDefault("xnxq01id", ""));
        appendParam(sb, "jx02id", formFields.getOrDefault("jx02id", ""));
        appendParam(sb, "pj02id", formFields.getOrDefault("pj02id", ""));
        appendParam(sb, "xh", formFields.getOrDefault("xh", ""));

        // 2. 对每个指标：pj06xh, pj0601id_{index}, 及所有 pj0601fz_{index}_{optionId}
        for (EvaluationIndicator indicator : indicators) {
            int idx = indicator.index();
            appendParam(sb, "pj06xh", String.valueOf(idx));

            String selectedId = indicatorSelections.get(idx);
            if (selectedId != null && !selectedId.isEmpty()) {
                appendParam(sb, "pj0601id_" + idx, selectedId);
            }

            // 发送该指标全部选项的分值
            for (EvaluationIndicatorOption option : indicator.options()) {
                appendParam(sb, "pj0601fz_" + idx + "_" + option.optionId(), option.score());
            }
        }

        // 3. isxtjg
        appendParam(sb, "isxtjg", String.valueOf(isxtjg));

        return sb.toString();
    }

    private static void appendParam(StringBuilder sb, String key, String value) {
        if (!sb.isEmpty()) {
            sb.append('&');
        }
        sb.append(Util.encodeUrl(key)).append('=').append(Util.encodeUrl(value));
    }

    /**
     * 构建评教表单页面的 Referer 查询参数字符串。
     * 教务系统通过 Referer 头校验 POST 请求是否来自合法的表单页面。
     */
    public String refererQueryString() {
        StringBuilder sb = new StringBuilder();
        String[] refFields = {"pj0502id", "pj01id", "xnxq01id", "jx02id", "jx0404id", "xsflid", "jg0101id"};
        for (String field : refFields) {
            String value = formFields.getOrDefault(field, "");
            if (!value.isEmpty()) {
                if (!sb.isEmpty()) sb.append('&');
                sb.append(field).append('=').append(Util.encodeUrl(value));
            }
        }
        return sb.toString();
    }

    public static final class Builder {
        private Map<String, String> formFields = new LinkedHashMap<>();
        private List<EvaluationIndicator> indicators = List.of();
        private final Map<Integer, String> indicatorSelections = new LinkedHashMap<>();
        private int isxtjg = 1;

        private Builder() {}

        /**
         * 从 {@link EvaluationFormData} 中提取表单字段和指标列表。
         */
        public Builder fromForm(EvaluationFormData formData) {
            Objects.requireNonNull(formData, "formData");
            this.formFields = new LinkedHashMap<>(formData.formFields());
            this.indicators = List.copyOf(formData.indicators());
            return this;
        }

        /**
         * 为指定指标设置评级。指标序号为 pj06xh 值（如 2, 3, 4 等）。
         */
        public Builder indicator(int index, EvaluationRating rating) {
            Objects.requireNonNull(rating, "rating");
            for (EvaluationIndicator indicator : indicators) {
                if (indicator.index() == index) {
                    indicator.getOption(rating).ifPresent(opt ->
                            indicatorSelections.put(index, opt.optionId()));
                    return this;
                }
            }
            throw new IllegalArgumentException("未找到指标序号: " + index);
        }

        /**
         * 通过语义化枚举为指定指标设置评级（推荐方式）。
         */
        public Builder indicator(EvaluationIndicatorType type, EvaluationRating rating) {
            return indicator(type.index, rating);
        }

        /**
         * 为所有未设置的指标统一设置评级。
         */
        public Builder indicatorAll(EvaluationRating rating) {
            Objects.requireNonNull(rating, "rating");
            for (EvaluationIndicator indicator : indicators) {
                if (!indicatorSelections.containsKey(indicator.index())) {
                    indicator.getOption(rating).ifPresent(opt ->
                            indicatorSelections.put(indicator.index(), opt.optionId()));
                }
            }
            return this;
        }

        public EvaluationSubmission build() {
            return new EvaluationSubmission(this);
        }
    }
}
