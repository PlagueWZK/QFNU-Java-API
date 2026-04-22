package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import io.github.plaguewzk.qfnujavaapi.model.exam.ExamSchedule;
import io.github.plaguewzk.qfnujavaapi.model.exam.ExamScheduleQuery;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2026/4/22 16:46
 *
 * @author PlagueWZK
 */

public class ExamScheduleService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<List<ExamSchedule>> examScheduleParser;

    public ExamScheduleService(QFNUContext context, HtmlParser<List<ExamSchedule>> examScheduleParser) {
        this.qfnuExecutor = context.executor();
        this.examScheduleParser = Objects.requireNonNull(examScheduleParser, "examScheduleParser");
    }

    public List<ExamSchedule> getExamSchedules() {
        return getExamSchedules(ExamScheduleQuery.builder().xnxqid(Term.current()).build());
    }

    public List<ExamSchedule> getExamSchedules(ExamScheduleQuery examScheduleQuery) {
        String html = qfnuExecutor.executePost(QFNUAPI.EXAM_INFORMATION_LIST, examScheduleQuery.toMap(), QFNUAPI.INDEX);
        if (html.contains("未查询到数据")) {
            return List.of();
        }
        return examScheduleParser.parser(html);
    }
}
