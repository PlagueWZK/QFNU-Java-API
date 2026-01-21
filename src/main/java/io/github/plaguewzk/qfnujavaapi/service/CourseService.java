package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.entity.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.entity.Term;
import io.github.plaguewzk.qfnujavaapi.model.entity.WeeklySchedule;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseTableParse;
import io.github.plaguewzk.qfnujavaapi.parser.impl.SjmsParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.WeeklyScheduleParser;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created on 2026/1/2 23:16
 *
 * @author PlagueWZK
 */
@RequiredArgsConstructor
public class CourseService {
    private final QFNUExecutor qfnuExecutor;
    private final WeeklyScheduleParser weeklyScheduleParser = new WeeklyScheduleParser();
    private final CourseTableParse courseTableParse = new CourseTableParse();
    private final SjmsParser sjmsParser = new SjmsParser();

    private String sjmsValueCache;

    public CourseTable getCourseTable(Term term, int week) {
        String result = qfnuExecutor.executePost(QFNUAPI.STUDENT_COURSE_LIST, Map.of("zc", String.valueOf(week), "xnxq01id", term.toString()), QFNUAPI.INDEX);
        return courseTableParse.parser(result);
    }

    public CourseTable getCurrentCourseTable() {
        return courseTableParse.parser(
                qfnuExecutor.executeGet(QFNUAPI.STUDENT_COURSE_LIST)
        );
    }

    public WeeklySchedule getCurrentWeeklyScheduleFromMainPage() {
        return getWeeklyScheduleFromMainPage(LocalDate.now());
    }

    public WeeklySchedule getWeeklyScheduleFromMainPage(LocalDate date) {
        sjmsValueCache = getOrFetchSjms();
        String html = qfnuExecutor.executePost(QFNUAPI.MAIN_INDEX_LOAD_COURSE, Map.of("rq", date.format(DateTimeFormatter.ISO_LOCAL_DATE), "sjmsValue", sjmsValueCache), QFNUAPI.MAIN_NEW_PAGE, Map.of("t1", "1"));
        return weeklyScheduleParser.parser(html);
    }

    private String getOrFetchSjms() {
        if (sjmsValueCache != null && !sjmsValueCache.isBlank()) {
            return sjmsValueCache;
        }
        return sjmsParser.parser(qfnuExecutor.executeGet(QFNUAPI.MAIN_NEW_PAGE, Map.of("t1", "1")));
    }
}
