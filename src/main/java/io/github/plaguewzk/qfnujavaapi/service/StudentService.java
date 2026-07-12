package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.evaluation.*;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 学生相关服务，包含学生信息、评价列表、评教表单、评教提交等功能。
 *
 * @author PlagueWZK
 */
@Slf4j
public class StudentService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<StudentInfo> infoParser;
    private final HtmlParser<List<EvaluationEntry>> evaluationListParser;
    private final HtmlParser<List<EvaluationCourse>> evaluationCourseParser;
    private final HtmlParser<EvaluationFormData> evaluationFormParser;

    public StudentService(QFNUContext context,
                          HtmlParser<StudentInfo> infoParser,
                          HtmlParser<List<EvaluationEntry>> evaluationListParser,
                          HtmlParser<List<EvaluationCourse>> evaluationCourseParser,
                          HtmlParser<EvaluationFormData> evaluationFormParser) {
        this.qfnuExecutor = context.executor();
        this.infoParser = Objects.requireNonNull(infoParser, "infoParser");
        this.evaluationListParser = Objects.requireNonNull(evaluationListParser, "evaluationListParser");
        this.evaluationCourseParser = Objects.requireNonNull(evaluationCourseParser, "evaluationCourseParser");
        this.evaluationFormParser = Objects.requireNonNull(evaluationFormParser, "evaluationFormParser");
    }

    public StudentInfo getStudentInfo() {
        String html = qfnuExecutor.executeGet(QFNUAPI.MAIN_NEW_PAGE, Map.of("t1", "1"));
        return infoParser.parser(html);
    }

    /**
     * 获取学生评价入口列表。
     *
     * @return 评价入口列表，若无待评价项目则返回空列表
     */
    public List<EvaluationEntry> getEvaluationList() {
        String html = qfnuExecutor.executeGet(QFNUAPI.STUDENT_FEEDBACK);
        return evaluationListParser.parser(html);
    }

    /**
     * 根据评价入口获取该批次下的待评课程列表。
     *
     * @param entry 评价入口（来自 {@link #getEvaluationList()}）
     * @return 待评课程列表，若无课程则返回空列表
     */
    public List<EvaluationCourse> getEvaluationCourses(EvaluationEntry entry) {
        Objects.requireNonNull(entry, "entry");

        EvaluationCourseQuery query = EvaluationCourseQuery.from(entry);
        String html = qfnuExecutor.executeGet(QFNUAPI.STUDENT_EVALUATION_COURSES, query.toMap());
        return evaluationCourseParser.parser(html);
    }

    /**
     * 获取指定课程的评教表单，包含课程信息、评价指标及所有选项。
     *
     * @param course 待评课程（来自 {@link #getEvaluationCourses(EvaluationEntry)}）
     * @return 评教表单数据
     */
    public EvaluationFormData getEvaluationForm(EvaluationCourse course) {
        Objects.requireNonNull(course, "course");

        Map<String, String> params = parseQueryString(course.evalUrl());
        String html = qfnuExecutor.executeGet(QFNUAPI.STUDENT_EVALUATION_FORM, params);
        return evaluationFormParser.parser(html);
    }

    /**
     * 提交评教表单。
     *
     * @param submission 评教提交参数
     */
    public void submitEvaluation(EvaluationSubmission submission) {
        Objects.requireNonNull(submission, "submission");

        // 构建带完整查询参数的 Referer URL，模拟真实浏览器行为
        String referer = QFNUAPI.STUDENT_EVALUATION_FORM.value
                + "?" + submission.refererQueryString();

        qfnuExecutor.executeFormPost(
                QFNUAPI.STUDENT_EVALUATION_SAVE,
                submission.toQueryString(),
                referer
        );
    }

    /**
     * 为单个课程自动评教并保存分数（不最终提交）。
     *
     * @param course 待评课程
     * @param scheme 评分方案
     * @return 评教结果
     */
    public EvaluationResult autoEvaluate(EvaluationCourse course, EvaluationScheme scheme) {
        Objects.requireNonNull(course, "course");
        Objects.requireNonNull(scheme, "scheme");

        try {
            EvaluationFormData formData = getEvaluationForm(course);
            Map<Integer, EvaluationRating> ratingMap = computeScheme(formData, scheme);
            EvaluationSubmission submission = buildSubmissionFromScheme(formData, ratingMap);
            submitEvaluation(submission);
            double score = EvaluationAutoScorer.computeTotalScore(formData, ratingMap);
            return EvaluationResult.success(course, scheme, score);
        } catch (Exception e) {
            log.error("自动评教失败: {} [{}]", course.courseName(), scheme, e);
            return EvaluationResult.failure(course, scheme,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * 一键评完一个评价入口下的所有课程（强制评教，不过滤已评课程）。
     *
     * <p>规则：
     * <ul>
     *   <li>得分 ≥ 90 的课程数量不超过总数 40%（向下取整）</li>
     *   <li>按课程列表顺序，前 M 个使用 {@link EvaluationScheme#CLOSEST_TO_FULL}（约 98 分）</li>
     *   <li>剩余课程使用 {@link EvaluationScheme#CLOSEST_TO_90}（约 90 分以下）</li>
     *   <li>每个课程评分包裹在 try-catch 中，失败不影响后续课程</li>
     *   <li>所有课程评分后不自动最终提交，需手动调用 {@link #finalSubmit(EvaluationEntry)}</li>
     * </ul>
     *
     * @param entry 评价入口
     * @return 评教结果列表
     */
    public List<EvaluationResult> autoEvaluateAll(EvaluationEntry entry) {
        Objects.requireNonNull(entry, "entry");

        List<EvaluationCourse> courses = getEvaluationCourses(entry);
        if (courses.isEmpty()) {
            return List.of();
        }

        int total = courses.size();
        int highScoreCount = (int) (total * 0.4); // 向下取整

        List<EvaluationResult> results = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            EvaluationCourse course = courses.get(i);
            EvaluationScheme scheme = i < highScoreCount
                    ? EvaluationScheme.CLOSEST_TO_FULL
                    : EvaluationScheme.CLOSEST_TO_90;

            results.add(autoEvaluate(course, scheme));
        }
        return results;
    }

    /**
     * 最终提交评教（在所有课程评分保存后手动调用，提交后不可修改）。
     *
     * @param entry 评价入口
     */
    public void finalSubmit(EvaluationEntry entry) {
        Objects.requireNonNull(entry, "entry");

        List<EvaluationCourse> courses = getEvaluationCourses(entry);
        if (courses.isEmpty()) {
            return;
        }

        // 从任意课程的表单中提取 pj02id
        EvaluationFormData formData = getEvaluationForm(courses.get(0));

        String body = buildFinalSubmitBody(entry, formData, courses.size());
        qfnuExecutor.executeFormPost(
                QFNUAPI.STUDENT_EVALUATION_FINAL_SUBMIT,
                body,
                QFNUAPI.STUDENT_EVALUATION_FORM
        );
    }

    private static Map<Integer, EvaluationRating> computeScheme(EvaluationFormData formData, EvaluationScheme scheme) {
        return switch (scheme) {
            case CLOSEST_TO_FULL -> EvaluationAutoScorer.computeClosestToFull(formData);
            case CLOSEST_TO_90 -> EvaluationAutoScorer.computeClosestTo90(formData);
        };
    }

    private static EvaluationSubmission buildSubmissionFromScheme(EvaluationFormData formData,
                                                                   Map<Integer, EvaluationRating> ratingMap) {
        EvaluationSubmission.Builder builder = EvaluationSubmission.builder()
                .fromForm(formData);
        for (var entry : ratingMap.entrySet()) {
            builder.indicator(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * 构建 toSavepj03wjpj.do 的 POST 表单体。
     */
    private static String buildFinalSubmitBody(EvaluationEntry entry, EvaluationFormData formData, int courseCount) {
        StringBuilder sb = new StringBuilder();

        appendF(sb, "cj0701id", "");
        appendF(sb, "pj0502id", nullToEmpty(entry.pj0502id()));
        appendF(sb, "pj05id", "");
        appendF(sb, "pj02id", formData.formFields().getOrDefault("pj02id", ""));
        appendF(sb, "zpjs", "");
        appendF(sb, "tjs", "");

        String pj01id = nullToEmpty(entry.pj01id());
        for (int i = 0; i < courseCount; i++) {
            appendF(sb, "pj01id", pj01id);
        }

        appendF(sb, "pageIndex", "1");
        return sb.toString();
    }

    private static void appendF(StringBuilder sb, String key, String value) {
        if (!sb.isEmpty()) {
            sb.append('&');
        }
        sb.append(io.github.plaguewzk.qfnujavaapi.util.Util.encodeUrl(key))
          .append('=')
          .append(io.github.plaguewzk.qfnujavaapi.util.Util.encodeUrl(value));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Map<String, String> parseQueryString(String url) {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        if (url == null || url.isBlank()) {
            return params;
        }
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return params;
        }
        String query = url.substring(queryStart + 1);
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return params;
    }
}
