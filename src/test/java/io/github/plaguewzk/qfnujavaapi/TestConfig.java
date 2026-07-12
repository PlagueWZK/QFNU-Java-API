package io.github.plaguewzk.qfnujavaapi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 测试配置工具类，从项目根目录的 config.qfnuapi.properties 加载凭据。
 *
 * @author PlagueWZK
 */
public final class TestConfig {

    private static final String CONFIG_FILE = "config.qfnuapi.properties";
    private static final Properties props = new Properties();
    private static final boolean loaded;

    static {
        boolean temp = false;
        Path configPath = Paths.get(CONFIG_FILE);
        try {
            if (Files.exists(configPath)) {
                try (InputStream is = Files.newInputStream(configPath)) {
                    props.load(is);
                    temp = true;
                }
            }
        } catch (IOException ignored) {
            // 配置文件不可用
        }
        loaded = temp;
    }

    private TestConfig() {}

    public static String getAccount() {
        return props.getProperty("qfnu.account", "");
    }

    public static String getPassword() {
        return props.getProperty("qfnu.password", "");
    }

    /**
     * 配置文件是否存在且凭据有效。
     */
    public static boolean isAvailable() {
        return loaded && !getAccount().isBlank() && !getPassword().isBlank();
    }
}
