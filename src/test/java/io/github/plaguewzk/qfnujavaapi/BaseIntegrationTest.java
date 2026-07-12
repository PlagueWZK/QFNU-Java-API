package io.github.plaguewzk.qfnujavaapi;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

/**
 * 集成测试基类，使用 config.qfnuapi.properties 中的真实凭据构建 QFNUClient。
 * 通过 {@code @Tag("integration")} 标记，可与单元测试区分执行。
 *
 * @author PlagueWZK
 */
@Tag("integration")
public abstract class BaseIntegrationTest {

    protected static volatile QFNUClient client;
    private static volatile boolean initialized;

    @BeforeAll
    static void baseSetUp() {
        if (initialized) {
            return;
        }
        synchronized (BaseIntegrationTest.class) {
            if (initialized) {
                return;
            }
            Assumptions.assumeTrue(TestConfig.isAvailable(),
                    "跳过集成测试：config.qfnuapi.properties 未找到或凭据为空");

            client = new QFNUClient.Builder()
                    .account(TestConfig.getAccount(), TestConfig.getPassword())
                    .build();
            initialized = true;
        }
    }
}
