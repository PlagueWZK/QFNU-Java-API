package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.model.grade.GradeReport;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradeReportParser implements HtmlParser<GradeReport> {
    private static final Pattern QUERY_CONDITION_PATTERN =
            Pattern.compile("查询条件：\\s*(.*?)\\s*(?=所修门数:|序号|$)");
    private static final Pattern TOTAL_COURSE_COUNT_PATTERN = Pattern.compile("所修门数:(\\d+)");
    private static final Pattern TOTAL_CREDITS_PATTERN = Pattern.compile("所修总学分:(\\d+(?:\\.\\d+)?)");
    private static final Pattern AVERAGE_GRADE_POINT_PATTERN = Pattern.compile("平均学分绩点:(\\d+(?:\\.\\d+)?)");
    private static final Pattern AVERAGE_SCORE_PATTERN = Pattern.compile("平均成绩:(\\d+(?:\\.\\d+)?)");

    private final CourseGradeParser courseGradeParser = new CourseGradeParser();

    @Override
    public GradeReport parser(String html) {
        Document document = Jsoup.parse(html);
        String normalizedText = normalizeWhitespace(document.text());
        List<CourseGrade> grades = courseGradeParser.parser(html);

        Integer totalCourseCount = parseInteger(normalizedText, TOTAL_COURSE_COUNT_PATTERN).orElse(grades.size());
        Double totalCredits = parseDouble(normalizedText, TOTAL_CREDITS_PATTERN).orElse(calculateTotalCredits(grades));
        Double averageCreditGradePoint = parseDouble(normalizedText, AVERAGE_GRADE_POINT_PATTERN)
                .orElse(calculateAverageCreditGradePoint(grades, totalCredits));
        Double averageScore = parseDouble(normalizedText, AVERAGE_SCORE_PATTERN)
                .orElse(calculateAverageScore(grades, totalCredits));

        return new GradeReport(
                parseQueryCondition(normalizedText),
                totalCourseCount,
                totalCredits,
                averageCreditGradePoint,
                averageScore,
                grades
        );
    }

    private String parseQueryCondition(String normalizedText) {
        return matchGroup(normalizedText, QUERY_CONDITION_PATTERN).orElse("");
    }

    private Optional<Integer> parseInteger(String text, Pattern pattern) {
        return matchGroup(text, pattern).map(Integer::valueOf);
    }

    private Optional<Double> parseDouble(String text, Pattern pattern) {
        return matchGroup(text, pattern).map(Double::valueOf);
    }

    private Optional<String> matchGroup(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Optional.ofNullable(matcher.group(1)).map(String::trim) : Optional.empty();
    }

    private String normalizeWhitespace(String text) {
        return text == null ? "" : text.replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
    }

    private Double calculateTotalCredits(List<CourseGrade> grades) {
        return round(
                grades.stream()
                        .map(CourseGrade::credit)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .sum()
        );
    }

    private Double calculateAverageCreditGradePoint(List<CourseGrade> grades, Double totalCredits) {
        if (totalCredits == null || totalCredits == 0D) {
            return 0D;
        }
        double weightedSum = grades.stream()
                .filter(grade -> grade.credit() != null && grade.gradePointAverage() != null)
                .mapToDouble(grade -> grade.credit() * grade.gradePointAverage())
                .sum();
        return round(weightedSum / totalCredits);
    }

    private Double calculateAverageScore(List<CourseGrade> grades, Double totalCredits) {
        if (totalCredits == null || totalCredits == 0D) {
            return 0D;
        }
        double weightedScore = grades.stream()
                .filter(grade -> grade.credit() != null)
                .mapToDouble(grade -> grade.credit() * resolveNumericScore(grade))
                .sum();
        return round(weightedScore / totalCredits);
    }

    private double resolveNumericScore(CourseGrade grade) {
        try {
            return Double.parseDouble(grade.grade());
        } catch (NumberFormatException ignored) {
            if (grade.gradePointAverage() != null) {
                return grade.gradePointAverage() * 10 + 50;
            }
            return 0D;
        }
    }

    private Double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
