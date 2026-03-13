package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.parser.impl.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一创建并缓存解析器实例，负责组合解析器之间的依赖关系。
 *
 * @author PlagueWZK
 */
public final class ParserFactory {
    private final ComponentResolver resolver;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, ComponentProvider<?>> parserRegistry;

    public ParserFactory(ComponentResolver resolver, Map<Class<?>, ComponentProvider<?>> parserRegistry) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.parserRegistry = Map.copyOf(Objects.requireNonNull(parserRegistry, "parserRegistry"));
    }

    public static void registerDefaults(ParserRegistry registry) {
        registry.registerParser(CourseParser.class, resolver -> new CourseParser());
        registry.registerParser(CourseInfoParser.class, resolver -> new CourseInfoParser());
        registry.registerParser(CourseGradeParser.class, resolver -> new CourseGradeParser());
        registry.registerParser(SjmsParser.class, resolver -> new SjmsParser());
        registry.registerParser(StudentInfoParser.class, resolver -> new StudentInfoParser());
        registry.registerParser(NotificationListParser.class, resolver -> new NotificationListParser());
        registry.registerParser(NotificationDetailParser.class, resolver -> new NotificationDetailParser());
        registry.registerParser(CourseTableParse.class, resolver -> new CourseTableParse(resolver.parser(CourseParser.class)));
        registry.registerParser(WeeklyScheduleParser.class, resolver -> new WeeklyScheduleParser(resolver.parser(CourseInfoParser.class)));
        registry.registerParser(GradeReportParser.class, resolver -> new GradeReportParser(resolver.parser(CourseGradeParser.class)));
    }

    public <T> T getParser(Class<T> parserType) {
        Object parser = cache.computeIfAbsent(parserType, this::createParser);
        return parserType.cast(parser);
    }

    private Object createParser(Class<?> parserType) {
        ComponentProvider<?> supplier = parserRegistry.get(parserType);
        if (supplier == null) {
            throw new IllegalArgumentException("不支持的解析器类型: " + parserType.getName());
        }
        return supplier.get(resolver);
    }
}
