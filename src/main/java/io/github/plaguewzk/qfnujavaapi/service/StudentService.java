package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;

import java.util.Map;
import java.util.Objects;

/**
 * Created on 2025/12/31 00:43
 *
 * @author PlagueWZK
 */
public class StudentService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<StudentInfo> infoParser;

    public StudentService(QFNUContext context, HtmlParser<StudentInfo> infoParser) {
        this.qfnuExecutor = context.executor();
        this.infoParser = Objects.requireNonNull(infoParser, "infoParser");
    }

    public StudentInfo getStudentInfo(){
        String html = qfnuExecutor.executeGet(QFNUAPI.MAIN_NEW_PAGE, Map.of("t1","1"));
        return infoParser.parser(html);
    }
}
