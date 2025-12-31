package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;

/**
 * Created on 2025/12/31 00:18
 *
 * @author PlagueWZK
 */

public interface HtmlParser<T> {

    /**
     * 将HTML解析为实体对象
     *
     * @param html 页面源码
     * @return 解析后的对象
     * @throws QFNUAPIException 如果解析过程中发生严重错误
     */
    T parser(String html);
}
