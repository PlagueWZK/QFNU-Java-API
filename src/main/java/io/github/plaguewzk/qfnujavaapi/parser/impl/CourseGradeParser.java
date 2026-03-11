package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.exception.ParsingErrorException;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import io.github.plaguewzk.qfnujavaapi.model.grade.AssessmentMethod;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseAttributes;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseGrade;
import io.github.plaguewzk.qfnujavaapi.model.grade.CourseNature;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created on 2026/3/11 17:20
 *
 * @author PlagueWZK
 */
public class CourseGradeParser implements HtmlParser<List<CourseGrade>> {

    @Override
    public List<CourseGrade> parser(String html) {
        Document doc = Jsoup.parse(html);
        Elements gradeRows = doc.select("table#dataList tr:has(> td)");
        ArrayList<CourseGrade> grades = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < gradeRows.size(); rowIndex++) {
            Element row = gradeRows.get(rowIndex);
            Elements cells = row.select("> td");
            grades.add(parseGradeRow(cells, rowIndex + 1));
        }
        return grades;
    }

    private CourseGrade parseGradeRow(Elements cells, int rowIndex) {
        Integer serialNumber = parseRequired(cells, 0, rowIndex, "序号", Integer::valueOf);
        Term startSemester = parseRequired(cells, 1, rowIndex, "开课学期", Term::parse);
        String courseId = parseRequired(cells, 2, rowIndex, "课程编号", Function.identity());
        String courseName = parseRequired(cells, 3, rowIndex, "课程名称", Function.identity());
        String groupName = parseOptional(cells, 4, rowIndex, "分组名", Function.identity());
        String grade = parseRequired(cells, 5, rowIndex, "成绩", Function.identity());
        String gradeSymbol = parseOptional(cells, 6, rowIndex, "成绩标识", Function.identity());
        Double credit = parseRequired(cells, 7, rowIndex, "学分", Double::valueOf);
        Integer classHours = parseRequired(cells, 8, rowIndex, "总学时", Integer::valueOf);
        Double gradePointAverage = parseRequired(cells, 9, rowIndex, "绩点", Double::valueOf);
        Term makeUpSemester = parseOptional(cells, 10, rowIndex, "补重学期", Term::parse);
        AssessmentMethod assessmentMethod = parseRequired(cells, 11, rowIndex, "考核方式", AssessmentMethod::fromDisplayName);
        String examinationNature = parseRequired(cells, 12, rowIndex, "考试性质", Function.identity());
        CourseAttributes courseAttributes = parseRequired(cells, 13, rowIndex, "课程属性", CourseAttributes::fromDisplayName);
        CourseNature courseNature = parseRequired(cells, 14, rowIndex, "课程性质", CourseNature::fromDisplayName);
        String courseCategories = parseOptional(cells, 15, rowIndex, "课程类别", Function.identity());

        return new CourseGrade(
                serialNumber,
                startSemester,
                courseId,
                courseName,
                groupName,
                grade,
                gradeSymbol,
                credit,
                classHours,
                gradePointAverage,
                makeUpSemester,
                assessmentMethod,
                examinationNature,
                courseAttributes,
                courseNature,
                courseCategories
        );
    }

    private Optional<String> getCellText(Elements cells, int index) {
        return Optional.ofNullable(index < cells.size() ? cells.get(index) : null)
                .map(Element::text)
                .map(String::trim)
                .filter(text -> !text.isBlank());
    }

    private <T> T parseRequired(Elements cells, int index, int rowIndex, String fieldName, Function<String, T> parser) {
        String rawValue = getCellText(cells, index)
                .orElseThrow(() -> buildMissingFieldException(rowIndex, fieldName, index));
        return convertValue(rawValue, rowIndex, fieldName, parser);
    }

    private <T> T parseOptional(Elements cells, int index, int rowIndex, String fieldName, Function<String, T> parser) {
        return getCellText(cells, index)
                .map(rawValue -> convertValue(rawValue, rowIndex, fieldName, parser))
                .orElse(null);
    }

    private <T> T convertValue(String rawValue, int rowIndex, String fieldName, Function<String, T> parser) {
        try {
            return parser.apply(rawValue);
        } catch (Exception exception) {
            throw new ParsingErrorException(buildFieldErrorMessage(rowIndex, fieldName, rawValue), exception);
        }
    }

    private ParsingErrorException buildMissingFieldException(int rowIndex, String fieldName, int index) {
        return new ParsingErrorException(
                "解析课程成绩时字段缺失: 第 " + rowIndex + " 行, 字段[" + fieldName + "], 列索引=" + index
        );
    }

    private String buildFieldErrorMessage(int rowIndex, String fieldName, String rawValue) {
        return "解析课程成绩时字段格式错误: 第 " + rowIndex + " 行, 字段[" + fieldName + "], 原始值=[" + rawValue + "]";
    }
}
