package io.github.plaguewzk.qfnujavaapi.core;

/**
 * 组件解析器，为注册函数提供上下文、parser 与 service 的访问能力。
 *
 * @author PlagueWZK
 */
public interface ComponentResolver {
    QFNUContext context();

    QFNUExecutor executor();

    <T> T service(Class<T> serviceType);

    <T> T parser(Class<T> parserType);
}
