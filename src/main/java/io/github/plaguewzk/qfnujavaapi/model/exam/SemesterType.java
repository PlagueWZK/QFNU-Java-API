package io.github.plaguewzk.qfnujavaapi.model.exam;

/**
 * Created on 2026/4/22 19:49
 *
 * @author PlagueWZK
 */

public enum SemesterType {
    BEGINNING_OF_TERM("期初", "1"),
    MID_TERM("期中", "2"),
    END_OF_TERM("期末", "3");

    public final String displayName;
    public final String value;

    SemesterType(String displayName, String value) {
        this.displayName = displayName;
        this.value = value;
    }

    public static SemesterType ofName(String displayName) {
        for (SemesterType value : values()) {
            if (displayName.equals(value.displayName)) return value;
        }
        return null;
    }

    public static SemesterType of(String value) {
        for (SemesterType v : values()) {
            if (value.equals(v.value)) return v;
        }
        return null;
    }
}
