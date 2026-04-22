package io.github.plaguewzk.qfnujavaapi.model.exam;

import io.github.plaguewzk.qfnujavaapi.model.course.Term;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created on 2026/4/22 23:56
 *
 * @author PlagueWZK
 */

public class ExamScheduleQueryTest {
    @Test
    public void shouldBuildDefaultQueryParameters() {
        ExamScheduleQuery query = ExamScheduleQuery.builder().build();

        assertNull(query.xqlbmc());
        assertNull(query.xqlb());
        assertEquals(
                Map.of(
                        "sxxnxq", "",
                        "dqxnxq", "",
                        "ckbz", "",
                        "xnxqid", Term.current().toString(),
                        "xqlb", "",
                        "xqlbmc", ""
                ),
                query.toMap()
        );
    }

    @Test
    public void shouldMapSemesterTypeAndExplicitParameters() {
        ExamScheduleQuery query = ExamScheduleQuery.builder()
                .xnxqid(new Term(2025, 1))
                .sxxnxq("2024-2025-2")
                .dqxnxq("2025-2026-1")
                .ckbz("1")
                .xqlb(SemesterType.END_OF_TERM)
                .build();

        assertEquals("期末", query.xqlbmc());
        assertEquals(SemesterType.END_OF_TERM, query.xqlb());
        assertEquals(
                Map.of(
                        "sxxnxq", "2024-2025-2",
                        "dqxnxq", "2025-2026-1",
                        "ckbz", "1",
                        "xnxqid", "2025-2026-1",
                        "xqlb", "3",
                        "xqlbmc", "期末"
                ),
                query.toMap()
        );
    }
}
