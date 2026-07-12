package io.github.plaguewzk.qfnujavaapi.model.evaluation;

import java.util.Objects;

/**
 * 学生评教课程列表中的一条待评课程。
 *
 * @param index              序号
 * @param courseId            课程编号
 * @param courseName          课程名称
 * @param teacher             授课教师
 * @param evalCategory        评教类别（如 评教课程）
 * @param totalScore          总评分
 * @param evaluated           已评（是/否）
 * @param submitted           是否提交（是/否）
 * @param lectureHours        讲课学时
 * @param practiceHours       实践学时
 * @param seminarHours        讲座学时
 * @param experimentHours     实验学时
 * @param designHours         设计学时
 * @param computerHours       其中上机学时
 * @param discussionHours     讨论辅导学时
 * @param extracurricularHours 课外学时
 * @param onlineHours         网络学时
 * @param evalUrl             评价页面的完整相对路径
 * @param jx02id              教学ID（课程标识）
 * @param jx0404id            教学任务ID
 * @param xsflid              学生分类ID
 * @param jg0101id            教师ID
 * @param zpf                 总评分参数
 */
public record EvaluationCourse(
        String index,
        String courseId,
        String courseName,
        String teacher,
        String evalCategory,
        String totalScore,
        String evaluated,
        String submitted,
        String lectureHours,
        String practiceHours,
        String seminarHours,
        String experimentHours,
        String designHours,
        String computerHours,
        String discussionHours,
        String extracurricularHours,
        String onlineHours,
        String evalUrl,
        String jx02id,
        String jx0404id,
        String xsflid,
        String jg0101id,
        String zpf
) {
    public EvaluationCourse {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(courseName, "courseName");
        Objects.requireNonNull(teacher, "teacher");
        Objects.requireNonNull(evalCategory, "evalCategory");
        Objects.requireNonNull(totalScore, "totalScore");
        Objects.requireNonNull(evaluated, "evaluated");
        Objects.requireNonNull(submitted, "submitted");
        // 学时和链接参数字段可为空字符串
    }
}
