package io.github.plaguewzk.qfnujavaapi.model.grade;

/**
 * Created on 2026/3/11 00:49
 *
 * @author PlagueWZK
 */

public enum AssessmentMethod {
    EXAMINATION("考试"),
    ASSESSMENT("考查"),
    UNDEFINED("未定义");
    public final String displayName;

    AssessmentMethod(String name) {
        displayName = name;
    }

    public static AssessmentMethod fromDisplayName(String displayName) {
        for (AssessmentMethod method : values()) {
            if (method.displayName.equals(displayName)) {
                return method;
            }
        }
        return null;
    }
}
