package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationEntry;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import io.github.plaguewzk.qfnujavaapi.parser.ParserUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 解析学生评价列表页面（xspj_find.do）。
 *
 * @author PlagueWZK
 */
public class EvaluationListParser implements HtmlParser<List<EvaluationEntry>> {

    // 解析进入评价链接中的参数
    private static final Pattern PJ0502ID_PATTERN = Pattern.compile("pj0502id=([^&]+)");
    private static final Pattern XNXQ01ID_PATTERN = Pattern.compile("xnxq01id=([^&]+)");
    private static final Pattern PJ01ID_PATTERN = Pattern.compile("pj01id=([^&]*)");

    private static final int COLUMN_COUNT = 7; // 序号, 学年学期, 评价分类, 评价批次, 开始时间, 结束时间, 操作

    @Override
    public List<EvaluationEntry> parser(String html) {
        if (html == null || html.isBlank()) {
            throw new PageStructureException("学生评价列表页面为空，无法解析");
        }

        Document document = Jsoup.parse(html);
        // 页面结构：div.Nsb_layout_r 内包含 table.Nsb_r_list
        Element layoutDiv = document.selectFirst("div.Nsb_layout_r");
        if (layoutDiv == null) {
            throw new PageStructureException("学生评价列表页面结构变化: 未找到 div.Nsb_layout_r");
        }

        Element table = layoutDiv.selectFirst("table.Nsb_r_list");
        if (table == null) {
            throw new PageStructureException("学生评价列表页面结构变化: 未找到 table.Nsb_r_list");
        }

        Elements rows = table.select("tr:has(> td)");
        List<EvaluationEntry> entries = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Elements cells = rows.get(rowIndex).select("> td");
            if (ParserUtils.isNoDataRow(cells)) {
                continue;
            }
            if (cells.size() < COLUMN_COUNT) {
                throw new ParsingErrorException(
                        "解析评价列表时字段缺失: 第 " + (rowIndex + 1) + " 行, 期望至少 " + COLUMN_COUNT
                                + " 列, 实际 " + cells.size() + " 列"
                );
            }

            String index = ParserUtils.getCellText(cells, 0, rowIndex, "序号");
            Term term = Term.parse(ParserUtils.getCellText(cells, 1, rowIndex, "学年学期"));
            String category = ParserUtils.getCellText(cells, 2, rowIndex, "评价分类");
            String batch = ParserUtils.getCellText(cells, 3, rowIndex, "评价批次");
            String startDate = ParserUtils.getCellText(cells, 4, rowIndex, "开始时间");
            String endDate = ParserUtils.getCellText(cells, 5, rowIndex, "结束时间");

            // 从操作列（第7列）的链接中提取参数
            Element link = cells.get(6).selectFirst("a[href]");
            String enterUrl = link != null ? link.attr("href") : "";
            String href = enterUrl;

            String pj0502id = ParserUtils.extractUrlParam(href, PJ0502ID_PATTERN);
            String xnxq01id = ParserUtils.extractUrlParam(href, XNXQ01ID_PATTERN);
            String pj01id = ParserUtils.extractUrlParam(href, PJ01ID_PATTERN);

            entries.add(new EvaluationEntry(
                    index, term, category, batch, startDate, endDate,
                    pj0502id, xnxq01id, pj01id, enterUrl
            ));
        }
        return entries;
    }
}
