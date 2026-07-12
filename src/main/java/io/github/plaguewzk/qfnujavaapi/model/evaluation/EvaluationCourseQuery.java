package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 评教课程查询参数。
 *
 * @author PlagueWZK
 */
@SuppressWarnings("SpellCheckingInspection")
public final class EvaluationCourseQuery {
    private final String pj0502id;
    private final String pj01id;
    private final String xnxq01id;

    private EvaluationCourseQuery(Builder builder) {
        this.pj0502id = normalize(builder.pj0502id);
        this.pj01id = normalize(builder.pj01id);
        this.xnxq01id = normalize(builder.xnxq01id);
    }

    /**
     * 从评价入口创建查询参数。
     *
     * @param entry 评价入口
     * @return 查询参数
     */
    public static EvaluationCourseQuery from(EvaluationEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return builder()
                .pj0502id(entry.pj0502id())
                .pj01id(entry.pj01id())
                .xnxq01id(entry.xnxq01id())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String pj0502id() {
        return pj0502id;
    }

    public String pj01id() {
        return pj01id;
    }

    public String xnxq01id() {
        return xnxq01id;
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<>();
        params.put("pj0502id", pj0502id);
        params.put("pj01id", pj01id);
        params.put("xnxq01id", xnxq01id);
        return params;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final class Builder {
        private String pj0502id = "";
        private String pj01id = "";
        private String xnxq01id = "";

        private Builder() {}

        public Builder pj0502id(String pj0502id) {
            this.pj0502id = pj0502id;
            return this;
        }

        public Builder pj01id(String pj01id) {
            this.pj01id = pj01id;
            return this;
        }

        public Builder xnxq01id(String xnxq01id) {
            this.xnxq01id = xnxq01id;
            return this;
        }

        public Builder xnxq01id(Term xnxq01id) {
            this.xnxq01id = xnxq01id != null ? xnxq01id.toString() : "";
            return this;
        }

        public EvaluationCourseQuery build() {
            return new EvaluationCourseQuery(this);
        }
    }
}
