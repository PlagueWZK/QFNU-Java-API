package io.github.plaguewzk.qfnujavaapi.model.grade;

/**
 * Created on 2026/3/11 20:38
 *
 * @author PlagueWZK
 */

public enum GradeDisplayMode {
    ALL("all"),
    MAX("max");

    public final String value;

    GradeDisplayMode(String value) {
        this.value = value;
    }
}
