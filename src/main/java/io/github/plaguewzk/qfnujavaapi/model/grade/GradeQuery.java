package io.github.plaguewzk.qfnujavaapi.model.grade;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 成绩查询参数。
 */
@SuppressWarnings("SpellCheckingInspection")
public final class GradeQuery {
    private final String kksj;
    private final String kcxz;
    private final String kcmc;
    private final String xsfs;

    private GradeQuery(Builder builder) {
        this.kksj = normalize(builder.kksj);
        this.kcxz = normalize(builder.kcxz);
        this.kcmc = normalize(builder.kcmc);
        this.xsfs = normalize(builder.xsfs);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static GradeQuery defaultQuery() {
        return builder().build();
    }

    public String kksj() {
        return kksj;
    }

    public String kcxz() {
        return kcxz;
    }

    public String kcmc() {
        return kcmc;
    }

    public String xsfs() {
        return xsfs;
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
