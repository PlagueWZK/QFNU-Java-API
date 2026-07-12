package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationFormData;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationIndicator;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationIndicatorOption;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationRating;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析评教表单页面（xspj_edit.do），提取课程信息、表单隐藏字段和所有评价指标。
 *
 * @author PlagueWZK
 */
public class EvaluationFormParser implements HtmlParser<EvaluationFormData> {

    private static final Pattern PJ06XH_PATTERN = Pattern.compile("pj0601id_(\\d+)");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("\\(\\d+\\)\\s*(.+?)（");
    private static final Pattern SCORE_PATTERN = Pattern.compile("课程名称：(.+?)\\s+评教大类：(.+?)\\s+总评分:\\s*(.+)");

    @Override
    public EvaluationFormData parser(String html) {
        if (html == null || html.isBlank()) {
            throw new PageStructureException("评教表单页面为空，无法解析");
        }

        Document document = Jsoup.parse(html);

        // 1. 提取表单隐藏字段
        Map<String, String> formFields = extractFormFields(document);

        // 2. 提取课程信息
        String[] courseInfo = extractCourseInfo(document);

        // 3. 提取评价指标
        List<EvaluationIndicator> indicators = extractIndicators(document);

        return new EvaluationFormData(
                courseInfo[0], courseInfo[1], courseInfo[2],
                formFields, indicators
        );
    }

    private Map<String, String> extractFormFields(Document document) {
        Map<String, String> fields = new LinkedHashMap<>();
        Element form = document.selectFirst("form#Form1");
        if (form == null) {
            throw new PageStructureException("评教表单页面结构变化: 未找到 form#Form1");
        }

        // 仅提取表单直系子元素中的隐藏字段（排除表格内的 pj06xh、pj0601fz 等）
        Elements hiddenInputs = form.select("> input[type=hidden]");
        for (Element input : hiddenInputs) {
            String name = input.attr("name");
            String value = input.attr("value");
            if (!name.isBlank()) {
                fields.put(name, value);
            }
        }
        return fields;
    }

    private String[] extractCourseInfo(Document document) {
        Element table = document.selectFirst("table#table1");
        if (table == null) {
            throw new PageStructureException("评教表单页面结构变化: 未找到 table#table1");
        }

        // 课程信息在第一个 th 中: "课程名称：XXX 评教大类：XXX 总评分: XXX"
        Element headerTh = table.selectFirst("tr th.Nsb_r_list_thb");
        if (headerTh == null) {
            // 回退：查找任意 th
            headerTh = table.selectFirst("th");
        }
        if (headerTh != null) {
            String headerText = headerTh.text().replace("&nbsp;", " ");
            Matcher m = SCORE_PATTERN.matcher(headerText);
            if (m.find()) {
                return new String[]{m.group(1).trim(), m.group(2).trim(), m.group(3).trim()};
            }
        }
        return new String[]{"", "", "0"};
    }

    private List<EvaluationIndicator> extractIndicators(Document document) {
        Element table = document.selectFirst("table#table1");
        if (table == null) {
            throw new PageStructureException("评教表单页面结构变化: 未找到 table#table1");
        }

        List<EvaluationIndicator> indicators = new ArrayList<>();
        String currentCategory = "";

        Elements rows = table.select("tr:has(> td)");
        for (Element row : rows) {
            Elements cells = row.select("> td");

            // 跳过大类汇总行（colspan="2"）
            if (cells.size() >= 1 && "2".equals(cells.get(0).attr("colspan"))) {
                currentCategory = extractCategoryName(cells.get(0).text());
                continue;
            }

            // 需要两个单元格：描述 + 选项
            if (cells.size() < 2) {
                continue;
            }

            // 检查第一个单元格是否包含 pj06xh
            Element pj06xhInput = cells.get(0).selectFirst("input[name=pj06xh]");
            if (pj06xhInput == null) {
                continue; // 非指标行（如表头、按钮行等）
            }

            int index;
            try {
                index = Integer.parseInt(pj06xhInput.attr("value"));
            } catch (NumberFormatException e) {
                continue;
            }

            // 提取描述文本（第一个 td 的文本，排除 pj06xh input 的值）
            String description = cells.get(0).text().trim();

            // 提取选项
            List<EvaluationIndicatorOption> options = extractOptions(cells.get(1), index);

            if (!options.isEmpty()) {
                indicators.add(new EvaluationIndicator(index, currentCategory, description, options));
            }
        }
        return indicators;
    }

    private List<EvaluationIndicatorOption> extractOptions(Element zbtd, int indicatorIndex) {
        List<EvaluationIndicatorOption> options = new ArrayList<>();
        List<Node> children = zbtd.childNodes();

        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (!(node instanceof Element e) || !"input".equals(e.tagName()) || !"radio".equals(e.attr("type"))) {
                continue;
            }

            String optionId = e.attr("value");
            if (optionId.isBlank()) {
                continue;
            }

            // 从紧跟的文本节点中提取标签
            String label = "";
            Node next = e.nextSibling();
            if (next instanceof TextNode tn) {
                String raw = tn.text().trim();
                // 提取 "优秀(10)" 格式
                int parenIdx = raw.indexOf('(');
                if (parenIdx > 0) {
                    int closeParen = raw.indexOf(')', parenIdx);
                    if (closeParen > parenIdx) {
                        label = raw.substring(0, closeParen + 1);
                    }
                }
                if (label.isEmpty()) {
                    label = raw;
                }
            }

            // 从后续的隐藏 input 中提取分值
            String score = "";
            for (int j = i + 1; j < children.size(); j++) {
                Node sibling = children.get(j);
                if (sibling instanceof Element se && "input".equals(se.tagName())
                        && "hidden".equals(se.attr("type"))
                        && se.attr("name").startsWith("pj0601fz_" + indicatorIndex + "_" + optionId)) {
                    score = se.attr("value");
                    break;
                }
            }

            EvaluationRating rating = EvaluationRating.fromLabel(label);

            options.add(new EvaluationIndicatorOption(
                    optionId,
                    label,
                    score.isEmpty() ? "0" : score,
                    rating != null ? rating : EvaluationRating.MEDIUM
            ));
        }
        return options;
    }

    private String extractCategoryName(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        Matcher m = CATEGORY_PATTERN.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return text.replaceAll("\\(\\d+\\)", "").replaceAll("（\\d+%）", "").trim();
    }
}
