package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeQuery;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseGradeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2026/3/11 00:06
 *
 * @author PlagueWZK
 */

public class GradeService {
    private final CourseGradeParser courseGradeParser;
    private final QFNUContext qfnuContext;
    public GradeService(QFNUContext qfnuContext) {
        this.qfnuContext = qfnuContext;
        courseGradeParser = new CourseGradeParser();
    }

    public List<CourseGrade> getGradeList() {
        return getGradeList(GradeQuery.defaultQuery());
    }

    public List<CourseGrade> getGradeList(GradeQuery query) {
        String s = qfnuContext.executor().executePost(QFNUAPI.GRADE_INQUIRY, query.toMap(), QFNUAPI.GRADE_INQUIRY);
        if (s.contains("学生个人考试成绩")){
            if (s.contains("未查询到数据")) return new ArrayList<>();
            return courseGradeParser.parser(s);
        }else {
            throw new PageStructureException("课程成绩解析页面结构变化,无法解析");
        }
    }
}
