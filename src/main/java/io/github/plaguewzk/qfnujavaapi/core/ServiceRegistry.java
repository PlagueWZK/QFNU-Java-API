package io.github.plaguewzk.qfnujavaapi.core;

import java.util.Map;

/**
 * service 注册表扩展点，供 SDK 内部和下游模块注册服务。
 *
 * @author PlagueWZK
 */
public interface ServiceRegistry {
    <T> void registerService(Class<T> serviceType, ComponentProvider<? extends T> provider);

    Map<Class<?>, ComponentProvider<?>> serviceProviders();
}
