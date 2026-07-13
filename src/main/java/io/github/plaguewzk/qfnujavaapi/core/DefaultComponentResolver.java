package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ComponentResolver} 的默认实现，同时承担 Parser 与 Service
 * 的按需创建和缓存职责。
 * <p>
 * 构造时即接收注册表，无需额外的 {@code bind()} 步骤，消除了与工厂类
 * 之间的相互引用和时序耦合。
 * <p>
 * 内置 ThreadLocal 创建链追踪，能精确检测并报告组件间的循环依赖。
 *
 * @author PlagueWZK
 */
public final class DefaultComponentResolver implements ComponentResolver {

    private final QFNUContext context;
    private final Map<Class<?>, ComponentProvider<?>> parserRegistry;
    private final Map<Class<?>, ComponentProvider<?>> serviceRegistry;
    private final Map<Class<?>, Object> parserCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> serviceCache = new ConcurrentHashMap<>();

    /**
     * 当前线程正在创建的组件类型栈，用于检测循环依赖。
     * 使用 {@link LinkedHashSet} 保证 O(1) 成员检测 + 保序遍历。
     */
    private final ThreadLocal<LinkedHashSet<Class<?>>> creationStack =
            ThreadLocal.withInitial(LinkedHashSet::new);

    public DefaultComponentResolver(QFNUContext context,
                                    Map<Class<?>, ComponentProvider<?>> parserRegistry,
                                    Map<Class<?>, ComponentProvider<?>> serviceRegistry) {
        this.context = Objects.requireNonNull(context, "context");
        this.parserRegistry = Map.copyOf(Objects.requireNonNull(parserRegistry, "parserRegistry"));
        this.serviceRegistry = Map.copyOf(Objects.requireNonNull(serviceRegistry, "serviceRegistry"));
    }

    @Override
    public QFNUContext context() {
        return context;
    }

    @Override
    public QFNUExecutor executor() {
        return context.executor();
    }

    @Override
    public <T> T parser(Class<T> parserType) {
        Object cached = parserCache.get(parserType);
        if (cached != null) {
            return parserType.cast(cached);
        }
        checkCircularDependency(parserType, "解析器");
        // 不使用 computeIfAbsent：它内置的递归检测会在 ThreadLocal
        // 检查之前触发，抛出无意义的 "Recursive update"。
        // get → create → putIfAbsent 让 ThreadLocal 精确控制检测时机。
        // 注意：get 和 putIfAbsent 不原子，多线程可能同时穿透缓存；
        // putIfAbsent + existing 判定确保最终只缓存第一个实例。
        Object created = createParser(parserType);
        Object existing = parserCache.putIfAbsent(parserType, created);
        return parserType.cast(existing != null ? existing : created);
    }

    @Override
    public <T> T service(Class<T> serviceType) {
        Object cached = serviceCache.get(serviceType);
        if (cached != null) {
            return serviceType.cast(cached);
        }
        checkCircularDependency(serviceType, "服务");
        // 同 parser()：get/putIfAbsent 不原子，多线程下依靠
        // putIfAbsent 返回值保证最终使用第一个成功缓存的实例。
        Object created = createService(serviceType);
        Object existing = serviceCache.putIfAbsent(serviceType, created);
        return serviceType.cast(existing != null ? existing : created);
    }

    private Object createParser(Class<?> parserType) {
        try {
            ComponentProvider<?> provider = parserRegistry.get(parserType);
            if (provider == null) {
                throw new IllegalArgumentException("不支持的解析器类型: " + parserType.getName());
            }
            return provider.get(this);
        } finally {
            creationStack.get().remove(parserType);
        }
    }

    private Object createService(Class<?> serviceType) {
        try {
            ComponentProvider<?> provider = serviceRegistry.get(serviceType);
            if (provider == null) {
                throw new ServiceCreationException("创建服务失败: 不支持的服务类型 " + serviceType.getName());
            }
            return provider.get(this);
        } finally {
            creationStack.get().remove(serviceType);
        }
    }

    /**
     * 检查当前类型是否已在创建链中。若是则抛出包含完整链路信息的异常。
     *
     * @param type  待创建的组件类型
     * @param kind  组件种类（"解析器" / "服务"），用于错误消息
     * @throws ServiceCreationException 如果检测到循环依赖
     */
    private void checkCircularDependency(Class<?> type, String kind) {
        LinkedHashSet<Class<?>> stack = creationStack.get();
        if (!stack.add(type)) {
            throw new ServiceCreationException(buildCycleMessage(stack, type, kind));
        }
    }

    private String buildCycleMessage(LinkedHashSet<Class<?>> stack, Class<?> repeated, String kind) {
        StringBuilder sb = new StringBuilder("检测到组件循环依赖，无法创建");
        sb.append(kind).append(" [").append(repeated.getSimpleName()).append("]\n");
        sb.append("创建链路: ");
        boolean inCycle = false;
        int depth = 0;
        for (Class<?> cls : stack) {
            if (cls == repeated) {
                if (inCycle) break; // 第二次遇到 → 循环部分已完整
                inCycle = true;
            }
            if (depth > 0) sb.append(" → ");
            sb.append(cls.getSimpleName());
            depth++;
        }
        sb.append(" → ").append(repeated.getSimpleName()).append(" (回到起点)");
        return sb.toString();
    }
}
