package io.github.plaguewzk.qfnujavaapi.model.entity;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Course(Weekday weekday, String courseName, Weeks weeks, Section section, String location, String teacher) {

    public enum Weekday {
        MONDAY(1, "星期一"),
        TUESDAY(2, "星期二"),
        WEDNESDAY(3, "星期三"),
        THURSDAY(4, "星期四"),
        FRIDAY(5, "星期五"),
        SATURDAY(6, "星期六"),
        SUNDAY(7, "星期日"),
        UNDEFINED(0, "未定义");

        private static final Map<Integer, Weekday> CACHE = new HashMap<>();

        static {
            for (Weekday weekday : values()) {
                CACHE.put(weekday.value, weekday);
            }
        }

        public final int value;
        private final String displayName;

        Weekday(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public static Weekday ofColumnIndex(int columnIndex) {
            Weekday weekday = CACHE.get(columnIndex);
            return Optional.ofNullable(weekday)
                    .orElseThrow(() -> new InvalidParameterException("未知星期列索引: " + columnIndex));
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum SectionConstant {
        S01(1), S02(2), S03(3), S04(4), S05(5), S06(6), S07(7), S08(8), S09(9), S10(10), S11(11), UNDEFINED(0);

        private static final Map<Integer, SectionConstant> CACHE = new HashMap<>();

        static {
            for (SectionConstant s : values()) {
                CACHE.put(s.value, s);
            }
        }

        public final int value;

        SectionConstant(int value) {
            this.value = value;
        }

        public static SectionConstant of(int value) {
            SectionConstant s = CACHE.get(value);
            return Optional.ofNullable(s).orElseThrow(() -> new InvalidParameterException("未知课程节数: " + value));
        }

        public String valueToString() {
            if (this == UNDEFINED) {
                return "UNDEFINED";
            }
            String s = String.valueOf(value);
            return s.length() == 1 ? "0" + value : s;
        }
    }

    public record Weeks(List<Integer> weeks) {
        public Weeks {
            if (weeks == null) {
                throw new InvalidParameterException("周数不能为空");
            }
            weeks = new ArrayList<>(weeks);
            for (Integer week : weeks) {
                if (week == null) {
                    throw new InvalidParameterException("周数列表中不能包含 null");
                }
                if (week < 1 || week > 20) {
                    throw new InvalidParameterException("周数必须在1-20之间");
                }
            }
            weeks.sort(Integer::compareTo);
            weeks = List.copyOf(weeks);
        }

        public Weeks(Integer... integers) {
            this(new ArrayList<>(List.of(integers)));
        }

        public static Weeks parse(String weeksStr) {
            if (weeksStr == null) {
                throw new InvalidParameterException("周数字符串不能为空");
            }
            List<Integer> weeksList = new ArrayList<>();
            try {
                String[] weeks = weeksStr.split(",");
                for (String week : weeks) {
                    String trimmedWeek = week.trim();
                    if (trimmedWeek.isEmpty()) {
                        continue;
                    }
                    if (trimmedWeek.contains("-")) {
                        String[] range = trimmedWeek.split("-");
                        if (range.length != 2) {
                            throw new InvalidParameterException("周次范围格式错误: " + trimmedWeek);
                        }
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int i = start; i <= end; i++) {
                            weeksList.add(i);
                        }
                    } else {
                        weeksList.add(Integer.parseInt(trimmedWeek));
                    }
                }
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("周次格式错误: " + weeksStr, e);
            }
            return new Weeks(weeksList);
        }

        private String[] normalize() {
            if (weeks.isEmpty()) {
                return new String[0];
            }
            StringBuilder builder = new StringBuilder();

            int startWeek = weeks.get(0);
            int endWeek = startWeek;

            if (weeks.size() == 1) {
                return new String[]{startWeek + "-" + endWeek};
            }

            for (int i = 1, len = weeks.size(); i < len; i++) {
                int week = weeks.get(i);
                if (week - endWeek > 1) {
                    builder.append(startWeek).append("-").append(endWeek).append("%");
                    startWeek = week;
                }
                endWeek = week;
                if (i == len - 1) {
                    builder.append(startWeek).append("-").append(endWeek);
                }
            }
            return builder.toString().split("%");
        }

        @NotNull
        @Override
        public String toString() {
            return String.join(",", normalize());
        }
    }

    public record Section(SectionConstant start, SectionConstant end) {
        private static final Pattern SECTION_PATTERN = Pattern.compile("\\[(\\d{1,2})(?:\\s*-\\s*(\\d{1,2}))*.*]");

        public Section {
            if (start == SectionConstant.UNDEFINED && end != SectionConstant.UNDEFINED) {
                throw new InvalidParameterException("未定义节数首尾必须相同");
            }
            if (start != SectionConstant.UNDEFINED && end == SectionConstant.UNDEFINED) {
                throw new InvalidParameterException("未定义节数首尾必须相同");
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
            if (matcher.find()) {
                try {
                    int startStr = Integer.parseInt(matcher.group(1));
                    int endStr = startStr;
                    if (matcher.group(2) != null) {
                        endStr = Integer.parseInt(matcher.group(2));
                    }
                    return new Section(SectionConstant.of(startStr), SectionConstant.of(endStr));
                } catch (QFNUAPIException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvalidParameterException("解析到的节次数值非法", e);
                }
            }
            return new Section();
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
}
