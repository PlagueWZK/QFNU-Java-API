package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用服务工厂：按需创建并缓存基于同一上下文的服务实例。
 *
 * @author PlagueWZK
 */
public final class ServiceFactory {
    private final QFNUContext context;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    public ServiceFactory(QFNUContext context) {
        this.context = context;
    }

    public <T> T getService(Class<T> serviceType) {
        Object service = cache.computeIfAbsent(serviceType, this::createService);
        return serviceType.cast(service);
    }

    private Object createService(Class<?> serviceType) {
        try {
            Constructor<?> constructor = serviceType.getConstructor(QFNUContext.class);
            return constructor.newInstance(context);
        } catch (ReflectiveOperationException e) {
            throw new ServiceCreationException("创建服务失败: " + serviceType.getName()
                    + "，请提供 public " + serviceType.getSimpleName() + "(QFNUContext) 构造器", e);
        }
    }
}
