package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.entity.Course;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;

/**
 * Created on 2026/1/19 01:45
 *
 * @author PlagueWZK
 */

public class CourseParser implements HtmlParser<Course> {
    /**
     * 解析单个课程
     *
     * @param html 包含单个课程信息的td标签html
     * @return 返回解析得到的Course对象
     */
    @Override
    public Course parser(String html) {

        return null;
    }
}
