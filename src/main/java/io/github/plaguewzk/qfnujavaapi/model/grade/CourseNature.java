package io.github.plaguewzk.qfnujavaapi.model.grade;

/**
 * Created on 2026/3/11 17:01
 *
 * @author PlagueWZK
 */

public enum CourseNature {
    PUBLIC_COURSE("公共课", "01"),

    PUBLIC_BASIC_COURSES("公共基础课", "02"),

    PROFESSIONAL_BASIC_COURSES("专业基础课", "03"),

    PROFESSIONAL_COURSES("专业课", "04"),

    PROFESSIONAL_ELECTIVE_COURSES("专业选修课", "05"),

    PUBLIC_ELECTIVE_COURSES("公共选修课", "06"),

    PROFESSIONAL_OPTIONAL_COURSES("专业任选课", "07"),

    PRACTICAL_TEACHING_SESSION("实践教学环节", "08"),

    PUBLIC_OPTIONAL_COURSES("公共任选课", "09"),

    FOUNDATIONS_OF_TEACHER_EDUCATION_REQUIRED("教师教育基础课程（必修）", "10"),

    REQUIRED_COURSES_FOR_MAJORS("专业必修课", "11"),

    COMPULSORY_COURSES_FOR_BASIC_DISCIPLINES("学科基础必修课", "12"),

    LIMITED_ELECTIVE_COURSES_IN_PROFESSIONAL_DIRECTION("专业方向限选课", "13"),

    EXAM_REGISTRATION_VIRTUAL_COURSE("考试报名虚拟课程", "14"),

    TEACHER_EDUCATION_ELECTIVE_COURSES("教师教育选修课程", "15"),

    PUBLIC_REQUIRED_COURSES("公共必修课", "16"),

    UNDEFINED("未定义", "");

    public final String displayName;
    public final String value;

    CourseNature(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
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

    public static CourseNature fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNDEFINED;
        }
        for (CourseNature courseNature : values()) {
            if (courseNature.value.equals(value)) {
                return courseNature;
            }
        }
        return UNDEFINED;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
