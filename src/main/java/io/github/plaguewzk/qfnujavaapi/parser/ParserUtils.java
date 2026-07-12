package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析器通用工具方法。
 *
 * @author PlagueWZK
 */
public final class ParserUtils {

    private ParserUtils() {}

    /**
     * 判断表格行是否为空数据行（提示"未查询到数据"等）。
     */
    public static boolean isNoDataRow(Elements cells) {
        if (cells.isEmpty()) {
            return true;
        }
        String text = cells.text();
        return text.contains("未查询到数据") || text.contains("没有相关数据");
    }

    /**
     * 从表格行中安全提取单元格文本。
     */
    public static String getCellText(Elements cells, int index, int rowIndex, String fieldName) {
        if (index >= cells.size()) {
            throw new ParsingErrorException(
                    "解析时字段缺失: 第 " + (rowIndex + 1) + " 行, 字段[" + fieldName + "], 列索引=" + index
            );
        }
        return cells.get(index).text().trim();
    }

    /**
     * 从 URL 中提取指定正则参数的值。
     */
    public static String extractUrlParam(String url, Pattern pattern) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }
}
