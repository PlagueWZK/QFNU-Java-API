package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                throw new InvalidParameterException("周数必须在 1-20 之间");
            }
        }
        weeks.sort(Integer::compareTo);
        weeks = List.copyOf(weeks);
    }

    public Weeks(Integer... values) {
        this(new ArrayList<>(List.of(values)));
    }

    public static Weeks parse(String weeksStr) {
        if (weeksStr == null) {
            throw new InvalidParameterException("周数字符串不能为空");
        }
        List<Integer> weeksList = new ArrayList<>();
        try {
            String[] items = weeksStr.split(",");
            for (String item : items) {
                String trimmedWeek = item.trim();
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

        for (int index = 1; index < weeks.size(); index++) {
            int week = weeks.get(index);
            if (week - endWeek > 1) {
                builder.append(startWeek).append("-").append(endWeek).append("%");
                startWeek = week;
            }
            endWeek = week;
            if (index == weeks.size() - 1) {
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
