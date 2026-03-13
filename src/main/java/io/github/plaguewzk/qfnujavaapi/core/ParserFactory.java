package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.model.course.CourseInfo;
import io.github.plaguewzk.qfnujavaapi.model.course.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.course.WeeklySchedule;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeReport;
import io.github.plaguewzk.qfnujavaapi.model.notification.Notification;
import io.github.plaguewzk.qfnujavaapi.model.notification.NotificationDetail;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseGradeParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseInfoParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseTableParse;
import io.github.plaguewzk.qfnujavaapi.parser.impl.GradeReportParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.NotificationDetailParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.NotificationListParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.SjmsParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.StudentInfoParser;
import io.github.plaguewzk.qfnujavaapi.parser.impl.WeeklyScheduleParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 统一创建并缓存解析器实例，负责组合解析器之间的依赖关系。
 *
 * @author PlagueWZK
 */
public final class ParserFactory {
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> parserRegistry;

    public ParserFactory() {
        this.parserRegistry = createRegistry();
    }

    public HtmlParser<CourseTable> courseTableParser() {
        return parser(CourseTableParse.class);
    }

    public HtmlParser<WeeklySchedule> weeklyScheduleParser() {
        return parser(WeeklyScheduleParser.class);
    }

    public HtmlParser<String> sjmsParser() {
        return parser(SjmsParser.class);
    }

    public HtmlParser<GradeReport> gradeReportParser() {
        return parser(GradeReportParser.class);
    }

    public HtmlParser<List<Notification>> notificationListParser() {
        return parser(NotificationListParser.class);
    }

    public HtmlParser<NotificationDetail> notificationDetailParser() {
        return parser(NotificationDetailParser.class);
    }

    public HtmlParser<StudentInfo> studentInfoParser() {
        return parser(StudentInfoParser.class);
    }

    private HtmlParser<List<CourseGrade>> courseGradeParser() {
        return parser(CourseGradeParser.class);
    }

    private HtmlParser<CourseInfo> courseInfoParser() {
        return parser(CourseInfoParser.class);
    }

    private CourseParser courseParser() {
        return parser(CourseParser.class);
    }

    private Map<Class<?>, Supplier<?>> createRegistry() {
        Map<Class<?>, Supplier<?>> registry = new HashMap<>();
        register(registry, CourseParser.class, CourseParser::new);
        register(registry, CourseInfoParser.class, CourseInfoParser::new);
        register(registry, CourseGradeParser.class, CourseGradeParser::new);
        register(registry, SjmsParser.class, SjmsParser::new);
        register(registry, StudentInfoParser.class, StudentInfoParser::new);
        register(registry, NotificationListParser.class, NotificationListParser::new);
        register(registry, NotificationDetailParser.class, NotificationDetailParser::new);
        register(registry, CourseTableParse.class, () -> new CourseTableParse(courseParser()));
        register(registry, WeeklyScheduleParser.class, () -> new WeeklyScheduleParser(courseInfoParser()));
        register(registry, GradeReportParser.class, () -> new GradeReportParser(courseGradeParser()));
        return Map.copyOf(registry);
    }

    @SuppressWarnings("unchecked")
    private <T> T parser(Class<?> parserType) {
        Supplier<?> supplier = parserRegistry.get(parserType);
        if (supplier == null) {
            throw new IllegalArgumentException("不支持的解析器类型: " + parserType.getName());
        }
        return (T) cache.computeIfAbsent(parserType, ignored -> supplier.get());
    }

    private <T> void register(Map<Class<?>, Supplier<?>> registry, Class<T> parserType, Supplier<? extends T> supplier) {
        registry.put(parserType, supplier);
    }
}
