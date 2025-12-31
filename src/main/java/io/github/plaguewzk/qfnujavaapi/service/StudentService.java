package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.entity.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.impl.StudentInfoParser;
import lombok.RequiredArgsConstructor;

/**
 * Created on 2025/12/31 00:43
 *
 * @author PlagueWZK
 */
@RequiredArgsConstructor
public class StudentService {
    private final QFNUExecutor qfnuExecutor;
    private final StudentInfoParser infoParser = new StudentInfoParser();

    public StudentInfo getStudentInfo(){
        String html = qfnuExecutor.executeGet(QFNUAPI.MAIN_NEW_PAGE);
        return infoParser.parser(html);
    }
}
