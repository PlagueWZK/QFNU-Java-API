package io.github.plaguewzk.qfnujavaapi.core;

/**
 * SDK 外部扩展模块，可注册自定义 parser 与 service。
 *
 * @author PlagueWZK
 */
public interface QFNUModule {
    void configure(ParserRegistry parsers, ServiceRegistry services);
}
