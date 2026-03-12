package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseNature;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeQuery;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeReport;
import io.github.plaguewzk.qfnujavaapi.parser.impl.GradeReportParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2026/3/11 00:06
 *
 * @author PlagueWZK
 */

public class GradeService {
    private final GradeReportParser gradeReportParser;
    private final QFNUContext qfnuContext;

    public GradeService(QFNUContext qfnuContext) {
        this.qfnuContext = qfnuContext;
        gradeReportParser = new GradeReportParser();
    }

    public List<CourseGrade> getGradeList() {
        return getGradeList(GradeQuery.defaultQuery());
    }

    public List<CourseGrade> getGradeList(GradeQuery query) {
        return getGradeReport(query).grades();
    }

    public GradeReport getGradeReport() {
        return getGradeReport(GradeQuery.defaultQuery());
    }

    public GradeReport getGradeReport(GradeQuery query) {
        String html = qfnuContext.executor().executePost(QFNUAPI.GRADE_INQUIRY, query.toMap(), QFNUAPI.GRADE_INQUIRY);
        if (!html.contains("学生个人考试成绩")) {
            throw new PageStructureException("课程成绩解析页面结构变化,无法解析");
        }
        if (html.contains("未查询到数据")) {
            return new GradeReport(resolveQueryCondition(query), 0, 0D, 0D, 0D, new ArrayList<>());
        }
        return gradeReportParser.parser(html);
    }

    private String resolveQueryCondition(GradeQuery query) {
        if (query.kksj().isBlank() && query.kcxz().isBlank() && query.kcmc().isBlank()) {
            return "全部";
        }
        StringBuilder builder = new StringBuilder();
        if (!query.kksj().isBlank()) {
            builder.append("开课时间【").append(query.kksj()).append("】");
        }
        if (!query.kcxz().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append("课程性质【").append(resolveCourseNatureDisplayName(query.kcxz())).append("】");
        }
        if (!query.kcmc().isBlank()) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append("课程名称【").append(query.kcmc()).append("】");
        }
        return builder.toString();
    }

    private String resolveCourseNatureDisplayName(String value) {
        CourseNature courseNature = CourseNature.fromValue(value);
        return courseNature == CourseNature.UNDEFINED ? value : courseNature.displayName;
    }
}
