package io.github.plaguewzk.qfnujavaapi.model.course;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum SectionConstant {
    S01(1),
    S02(2),
    S03(3),
    S04(4),
    S05(5),
    S06(6),
    S07(7),
    S08(8),
    S09(9),
    S10(10),
    S11(11),
    S12(12),
    UNDEFINED(0);

    private static final Map<Integer, SectionConstant> CACHE = new HashMap<>();

    static {
        for (SectionConstant sectionConstant : values()) {
            CACHE.put(sectionConstant.value, sectionConstant);
        }
    }

    public final int value;

    SectionConstant(int value) {
        this.value = value;
    }

    public static SectionConstant of(int value) {
        return Optional.ofNullable(CACHE.get(value))
                .orElseThrow(() -> new InvalidParameterException("未知课程节数: " + value));
    }

    public String valueToString() {
        if (this == UNDEFINED) {
            return "UNDEFINED";
        }
        String text = String.valueOf(value);
        return text.length() == 1 ? "0" + value : text;
    }
}
