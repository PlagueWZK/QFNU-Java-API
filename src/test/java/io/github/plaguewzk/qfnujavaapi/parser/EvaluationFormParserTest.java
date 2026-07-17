package io.github.plaguewzk.qfnujavaapi.parser;

import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationFormData;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationIndicator;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.EvaluationRating;
import io.github.plaguewzk.qfnujavaapi.service.impl.EvaluationSubmission;
import io.github.plaguewzk.qfnujavaapi.parser.impl.EvaluationFormParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationFormParserTest {

    private static final String SAMPLE_FORM_HTML = """
            <form method="post" name="Form1" id="Form1" action="/jsxsd/xspj/xspj_save.do" target="ifrmHidden">
                <input type="hidden" name="issubmit" id="issubmit" value="0" />
                <input type="hidden" name="sfxyt" id="sfxyt" value="0" />
                <input type="hidden" name="pj09id" value="54D3119C7F8837F2E0633465A8C00204" />
                <input type="hidden" name="pj01id" value="0C6E4478243641DEB09512383F76A80C" />
                <input type="hidden" name="pj0502id" value="33E941C11E3E4AD2B52C0691D05E2C10" />
                <input type="hidden" name="jg0101id" value="20172820" />
                <input type="hidden" name="jx0404id" value="202520262000765" />
                <input type="hidden" name="xsflid" value="1" />
                <input type="hidden" name="xnxq01id" value="2025-2026-2" />
                <input type="hidden" name="jx02id" value="1B2886593A6B4C3EA5CEA3819B881BC8" />
                <input type="hidden" name="pj02id" value="D59C3ABCD0974992B0E58A0C27CC3737" />
                <input type="hidden" name="xh" value="" />

                <table id="table1" cellspacing="0" class="Nsb_r_list Nsb_table">
                    <tr>
                        <th align="center" class="Nsb_r_list_thb">
                            课程名称：软件体系结构与设计 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;评教大类：学生评教
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 总评分: 0
                        </th>
                        <th align="center" class="Nsb_r_list_thb">
                            评价选项
                        </th>
                    </tr>
                    <tr>
                        <td colspan="2" align="left">
                            (01)&nbsp;教学素养（20%）
                        </td>
                    </tr>
                    <tr>
                        <td>
                            &nbsp;&nbsp;&nbsp;&nbsp;师德师风10分：1.坚守政治方向，为人师表
                            <input type="hidden" name="pj06xh" value="2">
                        </td>
                        <td name="zbtd">
                            <input type="radio" name="pj0601id_2" value="6B04AA7F52F84B3A9FDEEBF378B0E6F4"> 优秀(10)
                            <input type="hidden" name="pj0601fz_2_6B04AA7F52F84B3A9FDEEBF378B0E6F4" value="10">
                            <input type="radio" name="pj0601id_2" value="98B07E40B9FF496E92F173D3CE2C2E68"> 良好(8)
                            <input type="hidden" name="pj0601fz_2_98B07E40B9FF496E92F173D3CE2C2E68" value="8">
                            <input type="radio" name="pj0601id_2" value="3E209B4BF5104BB3A5D9A1A126EC274D"> 中等(7)
                            <input type="hidden" name="pj0601fz_2_3E209B4BF5104BB3A5D9A1A126EC274D" value="7">
                            <input type="radio" name="pj0601id_2" value="5675DD5C95174C57B1C921014EC0D010"> 及格(6)
                            <input type="hidden" name="pj0601fz_2_5675DD5C95174C57B1C921014EC0D010" value="6">
                            <input type="radio" name="pj0601id_2" value="48A00F7985BC4E9CB80E5F6E93CD2229"> 不及格(4)
                            <input type="hidden" name="pj0601fz_2_48A00F7985BC4E9CB80E5F6E93CD2229" value="4">
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="left">
                            (02)&nbsp;教学过程（30%）
                        </td>
                    </tr>
                    <tr>
                        <td>
                            &nbsp;&nbsp;&nbsp;&nbsp;内容质量10分：1.符合教学大纲
                            <input type="hidden" name="pj06xh" value="7">
                        </td>
                        <td name="zbtd">
                            <input type="radio" name="pj0601id_7" value="4495A68F7B9E462399E41D0D9E56CDCC"> 优秀(10.2)
                            <input type="hidden" name="pj0601fz_7_4495A68F7B9E462399E41D0D9E56CDCC" value="10.2">
                            <input type="radio" name="pj0601id_7" value="9DB9C8F4598342B8B91667A035C07DE7"> 良好(8.16)
                            <input type="hidden" name="pj0601fz_7_9DB9C8F4598342B8B91667A035C07DE7" value="8.16">
                            <input type="radio" name="pj0601id_7" value="14F7650EA2974CAA85EF08E8D99EB9E6"> 中等(7.14)
                            <input type="hidden" name="pj0601fz_7_14F7650EA2974CAA85EF08E8D99EB9E6" value="7.14">
                            <input type="radio" name="pj0601id_7" value="E1ABB767FC6849F99A9D03F2C8DB0C2E"> 及格(6.12)
                            <input type="hidden" name="pj0601fz_7_E1ABB767FC6849F99A9D03F2C8DB0C2E" value="6.12">
                            <input type="radio" name="pj0601id_7" value="40E920DBF3E240CEBBA4DDA9CDBD6525"> 不及格(4.08)
                            <input type="hidden" name="pj0601fz_7_40E920DBF3E240CEBBA4DDA9CDBD6525" value="4.08">
                        </td>
                    </tr>
                </table>
            </form>
            """;

    @Test
    void shouldParseFormFields() {
        EvaluationFormParser parser = new EvaluationFormParser();
        EvaluationFormData data = parser.parser(SAMPLE_FORM_HTML);

        assertEquals("软件体系结构与设计", data.courseName());
        assertEquals("学生评教", data.evalCategory());
        assertEquals("0", data.totalScore());

        assertEquals("54D3119C7F8837F2E0633465A8C00204", data.formFields().get("pj09id"));
        assertEquals("0C6E4478243641DEB09512383F76A80C", data.formFields().get("pj01id"));
        assertEquals("33E941C11E3E4AD2B52C0691D05E2C10", data.formFields().get("pj0502id"));
        assertEquals("20172820", data.formFields().get("jg0101id"));
        assertEquals("202520262000765", data.formFields().get("jx0404id"));
        assertEquals("1B2886593A6B4C3EA5CEA3819B881BC8", data.formFields().get("jx02id"));
        assertEquals("D59C3ABCD0974992B0E58A0C27CC3737", data.formFields().get("pj02id"));
    }

    @Test
    void shouldParseIndicators() {
        EvaluationFormParser parser = new EvaluationFormParser();
        EvaluationFormData data = parser.parser(SAMPLE_FORM_HTML);

        assertEquals(2, data.indicators().size());

        // 第一个指标
        EvaluationIndicator i1 = data.indicators().get(0);
        assertEquals(2, i1.index());
        assertEquals("教学素养", i1.category());
        assertTrue(i1.description().contains("师德师风"));
        assertEquals(5, i1.options().size());

        // 检查选项
        assertTrue(i1.getOption(EvaluationRating.EXCELLENT).isPresent());
        assertEquals("6B04AA7F52F84B3A9FDEEBF378B0E6F4",
                i1.getOption(EvaluationRating.EXCELLENT).get().optionId());
        assertEquals("10", i1.getOption(EvaluationRating.EXCELLENT).get().score());

        assertTrue(i1.getOption(EvaluationRating.GOOD).isPresent());
        assertEquals("8", i1.getOption(EvaluationRating.GOOD).get().score());

        assertTrue(i1.getOption(EvaluationRating.FAIL).isPresent());
        assertEquals("4", i1.getOption(EvaluationRating.FAIL).get().score());

        // 第二个指标
        EvaluationIndicator i2 = data.indicators().get(1);
        assertEquals(7, i2.index());
        assertEquals("教学过程", i2.category());
        assertTrue(i2.description().contains("内容质量"));
        assertEquals(5, i2.options().size());

        // 分数带小数
        assertTrue(i2.getOption(EvaluationRating.EXCELLENT).isPresent());
        assertEquals("10.2", i2.getOption(EvaluationRating.EXCELLENT).get().score());
    }

    @Test
    void shouldBuildSubmissionQueryString() {
        EvaluationFormParser parser = new EvaluationFormParser();
        EvaluationFormData data = parser.parser(SAMPLE_FORM_HTML);

        EvaluationSubmission submission = EvaluationSubmission.builder()
                .fromForm(data)
                .indicatorAll(EvaluationRating.EXCELLENT)
                .build();

        String qs = submission.toQueryString();

        // 验证关键字段存在
        assertTrue(qs.contains("pj09id=54D3119C7F8837F2E0633465A8C00204"));
        assertTrue(qs.contains("pj0601id_2=6B04AA7F52F84B3A9FDEEBF378B0E6F4"));
        assertTrue(qs.contains("pj0601id_7=4495A68F7B9E462399E41D0D9E56CDCC"));

        // 验证所有分值都被发送
        assertTrue(qs.contains("pj0601fz_2_6B04AA7F52F84B3A9FDEEBF378B0E6F4=10"));
        assertTrue(qs.contains("pj0601fz_2_48A00F7985BC4E9CB80E5F6E93CD2229=4"));
        assertTrue(qs.contains("pj0601fz_7_4495A68F7B9E462399E41D0D9E56CDCC=10.2"));
    }

    @Test
    void shouldSupportCustomIndicatorSelection() {
        EvaluationFormParser parser = new EvaluationFormParser();
        EvaluationFormData data = parser.parser(SAMPLE_FORM_HTML);

        EvaluationSubmission submission = EvaluationSubmission.builder()
                .fromForm(data)
                .indicator(2, EvaluationRating.GOOD)
                .indicator(7, EvaluationRating.EXCELLENT)
                .build();

        String qs = submission.toQueryString();

        assertTrue(qs.contains("pj0601id_2=98B07E40B9FF496E92F173D3CE2C2E68"));
        assertTrue(qs.contains("pj0601id_7=4495A68F7B9E462399E41D0D9E56CDCC"));
    }

    @Test
    void shouldThrowWhenFormMissing() {
        EvaluationFormParser parser = new EvaluationFormParser();
        assertThrows(PageStructureException.class, () ->
                parser.parser("<html><body>empty</body></html>"));
    }

    @Test
    void shouldThrowWhenHtmlIsBlank() {
        EvaluationFormParser parser = new EvaluationFormParser();
        assertThrows(PageStructureException.class, () -> parser.parser(""));
    }

    @Test
    void shouldThrowForUnknownIndicatorIndex() {
        EvaluationFormParser parser = new EvaluationFormParser();
        EvaluationFormData data = parser.parser(SAMPLE_FORM_HTML);

        assertThrows(IllegalArgumentException.class, () ->
                EvaluationSubmission.builder()
                        .fromForm(data)
                        .indicator(999, EvaluationRating.EXCELLENT)
                        .build());
    }
}
