package io.github.plaguewzk.qfnujavaapi.core;

/**
 * parser 注册表扩展点，供 SDK 内部和下游模块注册解析器。
 *
 * @author PlagueWZK
 */
public interface ParserRegistry {
    <T> void registerParser(Class<T> parserType, ComponentProvider<? extends T> provider);
}
