package io.github.plaguewzk.qfnujavaapi.model.grade;

/**
 * Created on 2026/3/11 17:01
 *
 * @author PlagueWZK
 */

public enum CourseNature {
    PUBLIC_COURSE("公共课"),

    PUBLIC_BASIC_COURSES("公共基础课"),

    PROFESSIONAL_BASIC_COURSES("专业基础课"),

    PROFESSIONAL_COURSES("专业课"),

    PROFESSIONAL_ELECTIVE_COURSES("专业选修课"),

    PUBLIC_ELECTIVE_COURSES("公共选修课"),

    PROFESSIONAL_OPTIONAL_COURSES("专业任选课"),

    PRACTICAL_TEACHING_SESSION("实践教学环节"),

    PUBLIC_OPTIONAL_COURSES("公共任选课"),

    FOUNDATIONS_OF_TEACHER_EDUCATION_REQUIRED("教师教育基础课程（必修）"),

    REQUIRED_COURSES_FOR_MAJORS("专业必修课"),

    COMPULSORY_COURSES_FOR_BASIC_DISCIPLINES("学科基础必修课"),

    LIMITED_ELECTIVE_COURSES_IN_PROFESSIONAL_DIRECTION("专业方向限选课"),

    EXAM_REGISTRATION_VIRTUAL_COURSE("考试报名虚拟课程"),

    TEACHER_EDUCATION_ELECTIVE_COURSES("教师教育选修课程"),

    PUBLIC_REQUIRED_COURSES("公共必修课"),

    UNDEFINED("未定义");

    public final String displayName;

    CourseNature(String displayName) {
        this.displayName = displayName;
    }

    public static CourseNature fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return UNDEFINED;
        }
        for (CourseNature value : values()) {
            if (value.displayName.equals(displayName) || displayName.contains(value.displayName)) {
                return value;
            }
        }
        return UNDEFINED;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
