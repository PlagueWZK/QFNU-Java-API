package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Section(SectionConstant start, SectionConstant end) {
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\[(\\d{1,2})(?:\\s*-\\s*(\\d{1,2}))*.*]");

    public Section {
        if ((start == SectionConstant.UNDEFINED) != (end == SectionConstant.UNDEFINED)) {
            throw new InvalidParameterException("未定义节数时，起止节次必须同时为 UNDEFINED");
        }
    }

    public Section() {
        this(SectionConstant.UNDEFINED, SectionConstant.UNDEFINED);
    }

    public static Section parse(String sectionStr) {
        if (sectionStr == null || sectionStr.trim().isEmpty()) {
            return new Section();
        }
        Matcher matcher = SECTION_PATTERN.matcher(sectionStr);
        if (!matcher.find()) {
            return new Section();
        }
        try {
            int startValue = Integer.parseInt(matcher.group(1));
            int endValue = startValue;
            if (matcher.group(2) != null) {
                endValue = Integer.parseInt(matcher.group(2));
            }
            return new Section(SectionConstant.of(startValue), SectionConstant.of(endValue));
        } catch (QFNUAPIException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidParameterException("解析到的节次数值非法", exception);
        }
    }

    @NotNull
    @Override
    public String toString() {
        if (start == SectionConstant.UNDEFINED) {
            return "UNDEFINED";
        }
        if (start == end) {
            return start.valueToString();
        }
        return start.valueToString() + "-" + end.valueToString();
    }
}
