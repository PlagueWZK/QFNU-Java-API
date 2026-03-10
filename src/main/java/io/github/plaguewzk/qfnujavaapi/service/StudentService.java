package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.impl.StudentInfoParser;

import java.util.Map;

/**
 * Created on 2025/12/31 00:43
 *
 * @author PlagueWZK
 */
public class StudentService {
    private final QFNUContext context;
    private final QFNUExecutor qfnuExecutor;
    private final StudentInfoParser infoParser = new StudentInfoParser();

    public StudentService(QFNUContext context) {
        this.context = context;
        this.qfnuExecutor = context.executor();
    }

    public StudentInfo getStudentInfo(){
        String html = qfnuExecutor.executeGet(QFNUAPI.MAIN_NEW_PAGE, Map.of("t1","1"));
        return infoParser.parser(html);
    }
}
