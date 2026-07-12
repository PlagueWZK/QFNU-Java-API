package io.github.plaguewzk.qfnujavaapi.model;

/**
 * Created on 2026/5/14 17:03
 *
 * @author PlagueWZK
 */

public enum College {

    SCHOOL_OF_HISTORY_AND_CULTURE("历史文化学院"),
    SCHOOL_OF_PSYCHOLOGY("心理学院"),
    SCHOOL_OF_CYBERSECURITY("网络空间安全学院"),
    SCHOOL_OF_MARXISM("马克思主义学院"),
    SCHOOL_OF_MATHEMATICAL_SCIENCES("数学科学学院");

    public final String name;

    College(String name) {
        this.name = name;
    }
}
