package io.github.plaguewzk.qfnujavaapi.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * Created on 2026/1/2 02:16
 *
 * @author PlagueWZK
 */

public class HTMLUtil {
    public static String processContent(Element contentDiv) {
        if (contentDiv == null) {
            return "";
        }

        // 1. 获取原始 HTML
        // 注意：如果不做 absUrl 处理，这里的 <img src="..."> 拿到的就是网页源码里原本的样子
        // 如果网页里写的是相对路径（如 /upload/img.jpg），这里拿到的也是相对路径
        String html = contentDiv.html();

        // 2. 配置白名单 (Safelist)
        // 使用 relaxed 模式，允许常见的文本、图片、表格、链接标签
        Safelist safelist = Safelist.relaxed();

        // --- 关键配置：允许保留内联样式 ---
        // 很多学校/政府网站的表格样式都是写在 style 里的 (如 style="border:1px solid...")
        // 如果不加这一行，Jsoup 会把所有 style 属性全删掉，表格会很难看
        safelist.addAttributes(":all", "style");
        safelist.addAttributes(":all", "class"); // 如果需要保留 class 也可以加上

        // --- 关键配置：允许表格的废弃属性 ---
        // 你的示例 HTML 里使用了 width, border 等旧式属性，需显式允许
        safelist.addAttributes("table", "border", "width", "cellspacing", "cellpadding", "align");
        safelist.addAttributes("tr", "height", "align", "valign");
        safelist.addAttributes("td", "width", "height", "colspan", "rowspan", "align", "valign");
        safelist.addAttributes("th", "width", "height", "colspan", "rowspan");

        // 3. 执行清洗
        // 这步主要为了防止 XSS 攻击（比如去除 <script>、onclick 等），保留安全的 HTML 结构
        return Jsoup.clean(html, safelist);
    }
}
