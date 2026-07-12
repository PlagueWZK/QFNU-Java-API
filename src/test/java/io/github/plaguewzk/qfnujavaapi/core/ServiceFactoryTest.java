package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("服务工厂")
class ServiceFactoryTest {

    @Test
    @DisplayName("同一服务类型获取两次返回相同实例")
    void shouldCacheServiceInstances() {
        ServiceFactory serviceFactory = newServiceFactory(registry ->
                registry.registerService(DummyService.class, resolver -> new DummyService(resolver.context()))
        );

        DummyService first = serviceFactory.getService(DummyService.class);
        DummyService second = serviceFactory.getService(DummyService.class);

        assertSame(first, second);
    }

    @Test
    @DisplayName("未注册的服务构造器缺失时抛出 ServiceCreationException")
    void shouldThrowWhenServiceConstructorMissing() {
        ServiceFactory serviceFactory = newServiceFactory(registry -> {
        });

        assertThrows(ServiceCreationException.class, () -> serviceFactory.getService(InvalidService.class));
    }

    private static ServiceFactory newServiceFactory(Consumer<ServiceRegistry> registrar) {
        ComponentRegistry registry = new ComponentRegistry();
        registrar.accept(registry);

        DefaultComponentResolver resolver = new DefaultComponentResolver(newContext());
        ParserFactory parserFactory = new ParserFactory(resolver, registry.parserProviders());
        ServiceFactory serviceFactory = new ServiceFactory(resolver, registry.serviceProviders());
        resolver.bind(parserFactory, serviceFactory);
        return serviceFactory;
    }

    private static QFNUContext newContext() {
        return new QFNUContext(new QFNUExecutor(new OkHttpClient()), "student", "password", null);
    }

    public static final class DummyService {
        public DummyService(QFNUContext context) {
        }
    }

    public static final class InvalidService {
    }
}
