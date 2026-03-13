package io.github.plaguewzk.qfnujavaapi.core;

/**
 * 组件创建函数，用于延迟创建 parser/service 实例。
 *
 * @param <T> 组件类型
 * @author PlagueWZK
 */
@FunctionalInterface
public interface ComponentProvider<T> {
    T get(ComponentResolver resolver);
}
