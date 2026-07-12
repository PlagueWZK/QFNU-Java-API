package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.parser.impl.CourseInfoParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CourseInfoParser 单元测试。
 * 当前 parser 通过 Jsoup.parseBodyFragment 返回的 Document 检查 attr("title")，
 * 需要完整的 HTML 文档格式，body 片段无法通过 title 检查。
 *
 * @author PlagueWZK
 */
@DisplayName("课程详情解析器")
class CourseInfoParserTest {

    private final CourseInfoParser parser = new CourseInfoParser();

    @Test
    @DisplayName("HTML 为 null 或空字符串时抛出 ParsingErrorException")
    void shouldThrowWhenHtmlIsBlank() {
        assertThrows(ParsingErrorException.class, () -> parser.parser(null));
        assertThrows(ParsingErrorException.class, () -> parser.parser(""));
    }

    @Test
    @DisplayName("缺少 title 属性时抛出 ParsingErrorException")
    void shouldThrowWhenTitleMissing() {
        assertThrows(ParsingErrorException.class, () -> parser.parser("<div>empty</div>"));
    }
}
