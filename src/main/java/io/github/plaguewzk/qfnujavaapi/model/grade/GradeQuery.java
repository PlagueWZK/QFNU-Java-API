package io.github.plaguewzk.qfnujavaapi.model.grade;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 成绩查询参数。
 */
@SuppressWarnings("SpellCheckingInspection")
public record GradeQuery(String kksj, String kcxz, String kcmc, String xsfs) {

    public GradeQuery {
        kksj = normalize(kksj);
        kcxz = normalize(kcxz);
        kcmc = normalize(kcmc);
        xsfs = normalize(xsfs);
    }

    private GradeQuery(Builder builder) {
        this(builder.kksj, builder.kcxz, builder.kcmc, builder.xsfs);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GradeQuery defaultQuery() {
        return builder().build();
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("kksj", kksj);
        params.put("kcxz", kcxz);
        params.put("kcmc", kcmc);
        params.put("xsfs", xsfs);
        return params;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final class Builder {
        private String kksj = "";
        private String kcxz = "";
        private String kcmc = "";
        private String xsfs = "all";

        private Builder() {}

        public Builder startSemester(Term kksj) {
            this.kksj = kksj.toString();
            return this;
        }

        public Builder courseNature(CourseNature kcxz) {
            this.kcxz = kcxz.value;
            return this;
        }

        public Builder courseName(String kcmc) {
            this.kcmc = kcmc;
            return this;
        }

        public Builder displayMode(GradeDisplayMode xsfs) {
            this.xsfs = xsfs.value;
            return this;
        }

        public GradeQuery build() {
            return new GradeQuery(this);
        }
    }
}
