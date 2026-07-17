package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationEntry;
import io.github.plaguewzk.qfnujavaapi.parser.impl.EvaluationListParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationListParserTest {

    @Test
    void shouldParseSingleEvaluationEntry() {
        EvaluationListParser parser = new EvaluationListParser();
        String html = """
                <div class="Nsb_layout_r">
                <div class="Nsb_layout_r title">学生评价</div>
                <form action="" name="Form1" id="Form1" method="post">
                <table width="100%" border="0" cellspacing="0" cellpadding="0" class="Nsb_r_list Nsb_table">
                    <tr>
                        <th width="6%" class="Nsb_r_list_thb" scope="col">序号</th>
                        <th class="Nsb_r_list_thb" scope="col">学年学期</th>
                        <th class="Nsb_r_list_thb" scope="col">评价分类</th>
                        <th class="Nsb_r_list_thb" scope="col">评价批次</th>
                        <th class="Nsb_r_list_thb" scope="col">开始时间</th>
                        <th class="Nsb_r_list_thb" scope="col">结束时间</th>
                        <th class="Nsb_r_list_thb" scope="col">操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>2025-2026-2</td>
                        <td>学生评教</td>
                        <td>2025-2026-2学生评教</td>
                        <td>2026-06-29</td>
                        <td>2026-07-12</td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_list.do?pj0502id=33E941C11E3E4AD2B52C0691D05E2C10&pj01id=&xnxq01id=2025-2026-2" title="点击进入评价">进入评价</a>
                        </td>
                    </tr>
                </table>
                </form>
                </div>
                """;

        List<EvaluationEntry> entries = parser.parser(html);

        assertEquals(1, entries.size());
        EvaluationEntry entry = entries.get(0);
        assertEquals("1", entry.index());
        assertEquals(Term.parse("2025-2026-2"), entry.term());
        assertEquals("学生评教", entry.category());
        assertEquals("2025-2026-2学生评教", entry.batch());
        assertEquals("2026-06-29", entry.startDate());
        assertEquals("2026-07-12", entry.endDate());
        assertEquals("33E941C11E3E4AD2B52C0691D05E2C10", entry.pj0502id());
        assertEquals("2025-2026-2", entry.xnxq01id());
        assertEquals("", entry.pj01id());
        assertTrue(entry.enterUrl().contains("xspj_list.do"));
    }

    @Test
    void shouldParseMultipleEvaluationEntries() {
        EvaluationListParser parser = new EvaluationListParser();
        String html = """
                <div class="Nsb_layout_r">
                <table class="Nsb_r_list Nsb_table">
                    <tr>
                        <th>序号</th>
                        <th>学年学期</th>
                        <th>评价分类</th>
                        <th>评价批次</th>
                        <th>开始时间</th>
                        <th>结束时间</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>2025-2026-2</td>
                        <td>学生评教</td>
                        <td>2025-2026-2学生评教</td>
                        <td>2026-06-29</td>
                        <td>2026-07-12</td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_list.do?pj0502id=AAA111&pj01id=BBB222&xnxq01id=2025-2026-2">进入评价</a>
                        </td>
                    </tr>
                    <tr>
                        <td>2</td>
                        <td>2025-2026-2</td>
                        <td>毕业生调查问卷</td>
                        <td>2025-2026-2毕业生调查</td>
                        <td>2026-06-29</td>
                        <td>2026-07-12</td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_list.do?pj0502id=AAA111&pj01id=CCC333&xnxq01id=2025-2026-2">进入评价</a>
                        </td>
                    </tr>
                </table>
                </div>
                """;

        List<EvaluationEntry> entries = parser.parser(html);

        assertEquals(2, entries.size());
        assertEquals("学生评教", entries.get(0).category());
        assertEquals("BBB222", entries.get(0).pj01id());
        assertEquals("毕业生调查问卷", entries.get(1).category());
        assertEquals("CCC333", entries.get(1).pj01id());
    }

    @Test
    void shouldThrowWhenLayoutDivMissing() {
        EvaluationListParser parser = new EvaluationListParser();
        String html = "<html><body>empty</body></html>";

        assertThrows(PageStructureException.class, () -> parser.parser(html));
    }

    @Test
    void shouldThrowWhenTableMissing() {
        EvaluationListParser parser = new EvaluationListParser();
        String html = """
                <div class="Nsb_layout_r">
                <div>无表格内容</div>
                </div>
                """;

        assertThrows(PageStructureException.class, () -> parser.parser(html));
    }

    @Test
    void shouldThrowWhenHtmlIsBlank() {
        EvaluationListParser parser = new EvaluationListParser();
        assertThrows(PageStructureException.class, () -> parser.parser(""));
    }

    @Test
    void shouldSkipRowWithoutEnterLink() {
        // 即使操作列没有链接，解析器也应该安全处理
        EvaluationListParser parser = new EvaluationListParser();
        String html = """
                <div class="Nsb_layout_r">
                <table class="Nsb_r_list Nsb_table">
                    <tr>
                        <th>序号</th>
                        <th>学年学期</th>
                        <th>评价分类</th>
                        <th>评价批次</th>
                        <th>开始时间</th>
                        <th>结束时间</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>1</td>
                        <td>2025-2026-2</td>
                        <td>学生评教</td>
                        <td>2025-2026-2学生评教</td>
                        <td>2026-06-29</td>
                        <td>2026-07-12</td>
                        <td>
                            <a href="/jsxsd/xspj/xspj_list.do?pj0502id=TEST001&xnxq01id=2025-2026-2">进入评价</a>
                        </td>
                    </tr>
                </table>
                </div>
                """;

        List<EvaluationEntry> entries = parser.parser(html);

        assertEquals(1, entries.size());
        assertEquals("TEST001", entries.get(0).pj0502id());
    }
}
