package io.github.plaguewzk.qfnujavaapi.model.exam;

/**
 * 学生考试安排。
 *
 * @param index       序号
 * @param campus      校区
 * @param session     考试场次
 * @param courseId    课程编号
 * @param courseName  课程名称
 * @param instructor  授课教师
 * @param examTime    考试时间
 * @param examRoom    考场
 * @param seatNumber  座位号
 * @param admissionNo 准考证号
 * @param remarks     备注
 * @param operation   操作
 */
public record ExamSchedule(
        String index,
        String campus,
        String session,
        String courseId,
        String courseName,
        String instructor,
        String examTime,
        String examRoom,
        String seatNumber,
        String admissionNo,
        String remarks,
        String operation
) {
}
