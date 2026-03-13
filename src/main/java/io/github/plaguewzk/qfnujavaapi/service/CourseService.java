package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.course.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import io.github.plaguewzk.qfnujavaapi.model.course.WeeklySchedule;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * Created on 2026/1/2 23:16
 *
 * @author PlagueWZK
 */
public class CourseService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<WeeklySchedule> weeklyScheduleParser;
    private final HtmlParser<CourseTable> courseTableParser;
    private final HtmlParser<String> sjmsParser;

    private String sjmsValueCache;

    public CourseService(
            QFNUContext context,
            HtmlParser<WeeklySchedule> weeklyScheduleParser,
            HtmlParser<CourseTable> courseTableParser,
            HtmlParser<String> sjmsParser
    ) {
        this.qfnuExecutor = context.executor();
        this.weeklyScheduleParser = Objects.requireNonNull(weeklyScheduleParser, "weeklyScheduleParser");
        this.courseTableParser = Objects.requireNonNull(courseTableParser, "courseTableParser");
        this.sjmsParser = Objects.requireNonNull(sjmsParser, "sjmsParser");
    }

    public CourseTable getCourseTable(Term term, int week) {
        String result = qfnuExecutor.executePost(QFNUAPI.STUDENT_COURSE_LIST, Map.of("zc", String.valueOf(week), "xnxq01id", term.toString()), QFNUAPI.INDEX);
        return courseTableParser.parser(result);
    }

    public CourseTable getCurrentCourseTable() {
        return courseTableParser.parser(
                qfnuExecutor.executeGet(QFNUAPI.STUDENT_COURSE_LIST)
        );
    }
    @Deprecated
    public WeeklySchedule getCurrentWeeklyScheduleFromMainPage() {
        return getWeeklyScheduleFromMainPage(LocalDate.now());
    }

    @Deprecated
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
