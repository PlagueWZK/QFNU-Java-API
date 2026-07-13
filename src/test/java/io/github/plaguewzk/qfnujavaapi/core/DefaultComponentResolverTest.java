package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.ServiceCreationException;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultComponentResolver")
class DefaultComponentResolverTest {

    @Test
    @DisplayName("同一服务类型获取两次返回相同实例")
    void shouldCacheServiceInstances() {
        DefaultComponentResolver resolver = newResolver(registry ->
                registry.registerService(DummyService.class, r -> new DummyService(r.context()))
        );

        DummyService first = resolver.service(DummyService.class);
        DummyService second = resolver.service(DummyService.class);

        assertSame(first, second);
    }

    @Test
    @DisplayName("未注册的服务类型获取时抛出 ServiceCreationException")
    void shouldThrowWhenServiceNotRegistered() {
        DefaultComponentResolver resolver = newResolver(registry -> {
        });

        assertThrows(ServiceCreationException.class, () -> resolver.service(InvalidService.class));
    }

    @Test
    @DisplayName("解析器循环依赖时抛出清晰异常信息")
    void shouldDetectCircularParserDependency() {
        DefaultComponentResolver resolver = newResolver(
                parsers -> {
                    parsers.registerParser(ParserA.class, r -> new ParserA(r.parser(ParserB.class)));
                    parsers.registerParser(ParserB.class, r -> new ParserB(r.parser(ParserA.class)));
                },
                services -> {
                }
        );

        ServiceCreationException ex = assertThrows(ServiceCreationException.class,
                () -> resolver.parser(ParserA.class));

        String message = ex.getMessage();
        assertTrue(message.contains("循环依赖"), "应包含'循环依赖': " + message);
        assertTrue(message.contains("ParserA"), "应包含 ParserA: " + message);
        assertTrue(message.contains("ParserB"), "应包含 ParserB: " + message);
    }

    @Test
    @DisplayName("服务循环依赖时抛出清晰异常信息")
    void shouldDetectCircularServiceDependency() {
        DefaultComponentResolver resolver = newResolver(
                parsers -> {
                },
                services -> {
                    services.registerService(ServiceA.class,
                            r -> new ServiceA(r.service(ServiceB.class)));
                    services.registerService(ServiceB.class,
                            r -> new ServiceB(r.service(ServiceA.class)));
                }
        );

        ServiceCreationException ex = assertThrows(ServiceCreationException.class,
                () -> resolver.service(ServiceA.class));

        String message = ex.getMessage();
        assertTrue(message.contains("循环依赖"), "应包含'循环依赖': " + message);
        assertTrue(message.contains("ServiceA"), "应包含 ServiceA: " + message);
        assertTrue(message.contains("ServiceB"), "应包含 ServiceB: " + message);
    }

    @Test
    @DisplayName("正常依赖链不触发误报")
    void shouldNotThrowForNormalDependencyChain() {
        DefaultComponentResolver resolver = newResolver(
                parsers -> {
                    parsers.registerParser(ParserC.class, r -> new ParserC(r.parser(ParserD.class)));
                    parsers.registerParser(ParserD.class, r -> new ParserD());
                },
                services -> {
                }
        );

        assertDoesNotThrow(() -> {
            ParserC result = resolver.parser(ParserC.class);
            assertNotNull(result);
            assertNotNull(result.dependency);
        });
    }

    @Test
    @DisplayName("跨类型依赖链（Service → Parser → Service）可以检测")
    void shouldDetectCrossTypeCircularDependency() {
        DefaultComponentResolver resolver = newResolver(
                parsers -> {
                    parsers.registerParser(CycleParser.class,
                            r -> new CycleParser(r.service(CycleService.class)));
                },
                services -> {
                    services.registerService(CycleService.class,
                            r -> new CycleService(r.parser(CycleParser.class)));
                }
        );

        ServiceCreationException ex = assertThrows(ServiceCreationException.class,
                () -> resolver.service(CycleService.class));

        String message = ex.getMessage();
        assertTrue(message.contains("循环依赖"), "应包含'循环依赖': " + message);
        assertTrue(message.contains("CycleService"), "应包含 CycleService: " + message);
        assertTrue(message.contains("CycleParser"), "应包含 CycleParser: " + message);
        System.out.println(message);
    }

    private static DefaultComponentResolver newResolver(Consumer<ServiceRegistry> registrar) {
        return newResolver(parsers -> {}, registrar);
    }

    private static DefaultComponentResolver newResolver(Consumer<ParserRegistry> parsersRegistrar,
                                                         Consumer<ServiceRegistry> servicesRegistrar) {
        ComponentRegistry registry = new ComponentRegistry();
        parsersRegistrar.accept(registry);
        servicesRegistrar.accept(registry);
        return new DefaultComponentResolver(newContext(),
                registry.parserProviders(), registry.serviceProviders());
    }

    private static QFNUContext newContext() {
        return new QFNUContext(new QFNUExecutor(new OkHttpClient()), "student", "password", null);
    }

    // ---------- 测试用组件 ----------

    public static final class DummyService {
        public DummyService(QFNUContext context) {
        }
    }

    public static final class InvalidService {
    }

    static final class ParserA implements HtmlParser<String> {
        final HtmlParser<String> dependency;

        ParserA(HtmlParser<String> dependency) { this.dependency = dependency; }

        @Override
        public String parser(String html) { return dependency.parser(html); }
    }

    static final class ParserB implements HtmlParser<String> {
        final HtmlParser<String> dependency;

        ParserB(HtmlParser<String> dependency) { this.dependency = dependency; }

        @Override
        public String parser(String html) { return dependency.parser(html); }
    }

    static final class ParserC implements HtmlParser<String> {
        final HtmlParser<String> dependency;

        ParserC(HtmlParser<String> dependency) { this.dependency = dependency; }

        @Override
        public String parser(String html) { return dependency.parser(html); }
    }

    static final class ParserD implements HtmlParser<String> {
        @Override
        public String parser(String html) { return html; }
    }

    static final class ServiceA {
        ServiceA(ServiceB dependency) { }
    }

    static final class ServiceB {
        ServiceB(ServiceA dependency) { }
    }

    static final class CycleService {
        CycleService(HtmlParser<String> parser) { }
    }

    static final class CycleParser implements HtmlParser<String> {
        CycleParser(CycleService service) { }

        @Override
        public String parser(String html) { return html; }
    }
}
