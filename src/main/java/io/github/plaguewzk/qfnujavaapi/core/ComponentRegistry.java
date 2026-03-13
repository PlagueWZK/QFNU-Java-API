package io.github.plaguewzk.qfnujavaapi.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ComponentRegistry implements ParserRegistry, ServiceRegistry {
    private final Map<Class<?>, ComponentProvider<?>> parserProviders = new LinkedHashMap<>();
    private final Map<Class<?>, ComponentProvider<?>> serviceProviders = new LinkedHashMap<>();

    @Override
    public <T> void registerParser(Class<T> parserType, ComponentProvider<? extends T> provider) {
        register(parserProviders, parserType, provider, "parser");
    }

    @Override
    public <T> void registerService(Class<T> serviceType, ComponentProvider<? extends T> provider) {
        register(serviceProviders, serviceType, provider, "service");
    }

    public Map<Class<?>, ComponentProvider<?>> parserProviders() {
        return Map.copyOf(parserProviders);
    }

    public Map<Class<?>, ComponentProvider<?>> serviceProviders() {
        return Map.copyOf(serviceProviders);
    }

    private <T> void register(
            Map<Class<?>, ComponentProvider<?>> target,
            Class<T> type,
            ComponentProvider<? extends T> provider,
            String componentKind
    ) {
        Objects.requireNonNull(type, componentKind + "Type");
        Objects.requireNonNull(provider, componentKind + "Provider");
        if (target.putIfAbsent(type, provider) != null) {
            throw new IllegalStateException(componentKind + " 已注册: " + type.getName());
        }
    }
}
