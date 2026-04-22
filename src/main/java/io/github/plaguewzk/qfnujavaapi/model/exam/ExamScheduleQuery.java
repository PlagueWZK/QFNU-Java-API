package io.github.plaguewzk.qfnujavaapi.model.exam;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2026/4/22 19:44
 *
 * @author PlagueWZK
 */

@SuppressWarnings("SpellCheckingInspection")
public class ExamScheduleQuery {
    private final String xqlbmc;
    private final String sxxnxq;
    private final String dqxnxq;
    private final String ckbz;
    private final Term xnxqid;
    private final SemesterType xqlb;

    private ExamScheduleQuery(Builder builder) {
        sxxnxq = builder.sxxnxq;
        dqxnxq = builder.dqxnxq;
        ckbz = builder.ckbz;
        xnxqid = builder.xnxqid;
        xqlb = builder.xqlb;
        xqlbmc = xqlb == null ? null : xqlb.displayName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("sxxnxq", sxxnxq ==  null ? "" : sxxnxq);
        map.put("dqxnxq", dqxnxq ==   null ? "" : dqxnxq);
        map.put("ckbz", ckbz ==   null ? "" : ckbz);
        map.put("xnxqid", xnxqid == null ? Term.current().toString() : xnxqid.toString());
        map.put("xqlb", xqlb == null ? "" : xqlb.value);
        map.put("xqlbmc", xqlbmc == null ? "期末" : xqlbmc);
        return map;
    }


    public static class Builder {
        private String sxxnxq;
        private String dqxnxq;
        private String ckbz;
        private Term xnxqid;
        private SemesterType xqlb;

        private Builder() {
        }

        public Builder sxxnxq(String sxxnxq) {
            this.sxxnxq = sxxnxq;
            return this;
        }

        public Builder dqxnxq(String dqxnxq) {
            this.dqxnxq = dqxnxq;
            return this;
        }

        public Builder ckbz(String ckbz) {
            this.ckbz = ckbz;
            return this;
        }

        public Builder xnxqid(Term xnxqid) {
            this.xnxqid = xnxqid;
            return this;
        }

        public Builder xqlb(SemesterType xqlb) {
            this.xqlb = xqlb;
            return this;
        }

        public ExamScheduleQuery build() {
            return new ExamScheduleQuery(this);
        }
    }
}
