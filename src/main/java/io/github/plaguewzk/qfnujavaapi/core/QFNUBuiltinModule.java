package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.parser.impl.*;
import io.github.plaguewzk.qfnujavaapi.service.*;

/**
 * 官方内置模块，负责注册 SDK 所有默认的 Parser 和 Service。
 * <p>
 * 将默认组件的注册逻辑独立为单独的模块，遵循单一职责原则。
 *
 * @author PlagueWZK
 */
public final class QFNUBuiltinModule implements QFNUModule {

    @Override
    public void configure(ParserRegistry parsers, ServiceRegistry services) {
        registerParsers(parsers);
        registerServices(services);
    }

    private void registerParsers(ParserRegistry registry) {
        registry.registerParser(CourseParser.class, resolver -> new CourseParser());
        registry.registerParser(CourseInfoParser.class, resolver -> new CourseInfoParser());
        registry.registerParser(CourseGradeParser.class, resolver -> new CourseGradeParser());
        registry.registerParser(ExamScheduleParser.class, resolver -> new ExamScheduleParser());
        registry.registerParser(SjmsParser.class, resolver -> new SjmsParser());
        registry.registerParser(StudentInfoParser.class, resolver -> new StudentInfoParser());
        registry.registerParser(NotificationListParser.class, resolver -> new NotificationListParser());
        registry.registerParser(NotificationDetailParser.class, resolver -> new NotificationDetailParser());
        registry.registerParser(EvaluationListParser.class, resolver -> new EvaluationListParser());
        registry.registerParser(EvaluationCourseParser.class, resolver -> new EvaluationCourseParser());
        registry.registerParser(EvaluationFormParser.class, resolver -> new EvaluationFormParser());

        // 以下 Parser 依赖其他 Parser
        registry.registerParser(CourseTableParse.class,
                resolver -> new CourseTableParse(resolver.parser(CourseParser.class)));
        registry.registerParser(WeeklyScheduleParser.class,
                resolver -> new WeeklyScheduleParser(resolver.parser(CourseInfoParser.class)));
        registry.registerParser(GradeReportParser.class,
                resolver -> new GradeReportParser(resolver.parser(CourseGradeParser.class)));
    }

    private void registerServices(ServiceRegistry registry) {
        registry.registerService(LoginService.class,
                resolver -> new LoginService(resolver.context()));
        registry.registerService(CourseService.class,
                resolver -> new CourseService(
                        resolver.context(),
                        resolver.parser(WeeklyScheduleParser.class),
                        resolver.parser(CourseTableParse.class),
                        resolver.parser(SjmsParser.class)
                ));
        registry.registerService(ExamScheduleService.class,
                resolver -> new ExamScheduleService(
                        resolver.context(),
                        resolver.parser(ExamScheduleParser.class)
                ));
        registry.registerService(GradeService.class,
                resolver -> new GradeService(
                        resolver.context(),
                        resolver.parser(GradeReportParser.class)
                ));
        registry.registerService(NotificationService.class,
                resolver -> new NotificationService(
                        resolver.context(),
                        resolver.parser(NotificationListParser.class),
                        resolver.parser(NotificationDetailParser.class)
                ));
        registry.registerService(StudentService.class,
                resolver -> new StudentService(
                        resolver.context(),
                        resolver.parser(StudentInfoParser.class),
                        resolver.parser(EvaluationListParser.class),
                        resolver.parser(EvaluationCourseParser.class),
                        resolver.parser(EvaluationFormParser.class)
                ));
    }
}
