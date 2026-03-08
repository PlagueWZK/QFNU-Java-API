package io.github.plaguewzk.qfnujavaapi.model.entity;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public record Term(int startYear, int endYear, int termIndex) {
    public Term {
        if (endYear - startYear != 1) {
            throw new InvalidParameterException("学期id年份必须连续(如2025-2026)");
        }
        if (termIndex < 1 || termIndex > 2) {
            throw new InvalidParameterException("学期id必须为1或2");
        }
    }

    public Term(int startYear, int termIndex) {
        this(startYear, startYear + 1, termIndex);
    }

    public static Term parse(String termId) {
        if (termId == null || termId.isBlank()) {
            throw new InvalidParameterException("学期id不能为空");
        }
        try {
            String[] parts = termId.split("-");
            return new Term(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        } catch (Exception e) {
            throw new InvalidParameterException("学期id格式错误, 应该为yyyy-yyyy-n", e);
        }
    }

    public static Term current() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        return now.getMonthValue() >= 8 ? new Term(year, 1) : new Term(year - 1, 2);
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%d-%d-%d", startYear, endYear, termIndex);
    }
}
