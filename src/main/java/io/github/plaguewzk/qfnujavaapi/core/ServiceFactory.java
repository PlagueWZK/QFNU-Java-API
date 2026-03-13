package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;
import io.github.plaguewzk.qfnujavaapi.service.CourseService;
import io.github.plaguewzk.qfnujavaapi.service.GradeService;
import io.github.plaguewzk.qfnujavaapi.service.LoginService;
import io.github.plaguewzk.qfnujavaapi.service.NotificationService;
import io.github.plaguewzk.qfnujavaapi.service.StudentService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 通用服务工厂：按需创建并缓存基于同一上下文的服务实例。
 *
 * @author PlagueWZK
 */
public final class ServiceFactory {
    private final QFNUContext context;
    private final ParserFactory parserFactory;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> serviceRegistry;

    public ServiceFactory(QFNUContext context) {
        this(context, new ParserFactory());
    }

    ServiceFactory(QFNUContext context, ParserFactory parserFactory) {
        this.context = context;
        this.parserFactory = Objects.requireNonNull(parserFactory, "parserFactory");
        this.serviceRegistry = createRegistry();
    }

    public <T> T getService(Class<T> serviceType) {
        Object service = cache.computeIfAbsent(serviceType, this::createService);
        return serviceType.cast(service);
    }

    private Object createService(Class<?> serviceType) {
        Supplier<?> supplier = serviceRegistry.get(serviceType);
        if (supplier == null) {
            throw new ServiceCreationException("创建服务失败: 不支持的服务类型 " + serviceType.getName());
        }
        return supplier.get();
    }

    private Map<Class<?>, Supplier<?>> createRegistry() {
        Map<Class<?>, Supplier<?>> registry = new HashMap<>();
        register(registry, LoginService.class, () -> new LoginService(context));
        register(
                registry,
                CourseService.class,
                () -> new CourseService(
                        context,
                        parserFactory.weeklyScheduleParser(),
                        parserFactory.courseTableParser(),
                        parserFactory.sjmsParser()
                )
        );
        register(registry, GradeService.class, () -> new GradeService(context, parserFactory.gradeReportParser()));
        register(
                registry,
                NotificationService.class,
                () -> new NotificationService(
                        context,
                        parserFactory.notificationListParser(),
                        parserFactory.notificationDetailParser()
                )
        );
        register(registry, StudentService.class, () -> new StudentService(context, parserFactory.studentInfoParser()));
        return Map.copyOf(registry);
    }

    private <T> void register(Map<Class<?>, Supplier<?>> registry, Class<T> serviceType, Supplier<? extends T> supplier) {
        registry.put(serviceType, supplier);
    }
}
