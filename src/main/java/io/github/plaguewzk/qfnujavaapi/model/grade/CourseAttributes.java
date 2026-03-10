package io.github.plaguewzk.qfnujavaapi.model.grade;

/**
 * Created on 2026/3/11 00:53
 *
 * @author PlagueWZK
 */

public enum CourseAttributes {
    REQUIRED("必修"),
    ELECTIVE("任选"),
    GENERAL_ELECTIVE("公选"),
    UNDEFINED("未定义");

    public final String displayName;

    CourseAttributes(String displayName) {
        this.displayName = displayName;
    }
}
