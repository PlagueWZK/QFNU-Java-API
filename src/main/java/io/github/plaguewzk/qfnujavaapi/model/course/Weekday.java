package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        return Optional.ofNullable(CACHE.get(columnIndex))
                .orElseThrow(() -> new InvalidParameterException("未知星期列索引: " + columnIndex));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
