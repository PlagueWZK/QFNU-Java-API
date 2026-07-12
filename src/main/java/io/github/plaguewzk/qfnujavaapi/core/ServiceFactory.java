package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;
import io.github.plaguewzk.qfnujavaapi.parser.impl.*;
import io.github.plaguewzk.qfnujavaapi.service.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用服务工厂：按需创建并缓存基于同一上下文的服务实例。
 *
 * @author PlagueWZK
 */
public final class ServiceFactory {
    private final ComponentResolver resolver;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, ComponentProvider<?>> serviceRegistry;

    public ServiceFactory(ComponentResolver resolver, Map<Class<?>, ComponentProvider<?>> serviceRegistry) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.serviceRegistry = Map.copyOf(Objects.requireNonNull(serviceRegistry, "serviceRegistry"));
    }

    public static void registerDefaults(ServiceRegistry registry) {
        registry.registerService(LoginService.class, resolver -> new LoginService(resolver.context()));
        registry.registerService(
                CourseService.class,
                resolver -> new CourseService(
                        resolver.context(),
                        resolver.parser(WeeklyScheduleParser.class),
                        resolver.parser(CourseTableParse.class),
                        resolver.parser(SjmsParser.class)
                )
        );
        registry.registerService(
                ExamScheduleService.class,
                resolver -> new ExamScheduleService(
                        resolver.context(),
                        resolver.parser(ExamScheduleParser.class)
                )
        );
        registry.registerService(
                GradeService.class,
                resolver -> new GradeService(
                        resolver.context(),
                        resolver.parser(GradeReportParser.class)
                )
        );
        registry.registerService(
                NotificationService.class,
                resolver -> new NotificationService(
                        resolver.context(),
                        resolver.parser(NotificationListParser.class),
                        resolver.parser(NotificationDetailParser.class)
                )
        );
        registry.registerService(
                StudentService.class,
                resolver -> new StudentService(
                        resolver.context(),
                        resolver.parser(StudentInfoParser.class),
                        resolver.parser(EvaluationListParser.class),
                        resolver.parser(EvaluationCourseParser.class),
                        resolver.parser(EvaluationFormParser.class)
                )
        );
    }

    public <T> T getService(Class<T> serviceType) {
        Object service = cache.computeIfAbsent(serviceType, this::createService);
        return serviceType.cast(service);
    }

    private Object createService(Class<?> serviceType) {
        ComponentProvider<?> supplier = serviceRegistry.get(serviceType);
        if (supplier == null) {
            throw new ServiceCreationException("创建服务失败: 不支持的服务类型 " + serviceType.getName());
        }
        return supplier.get(resolver);
    }
}
