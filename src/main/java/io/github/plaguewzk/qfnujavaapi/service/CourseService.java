package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.model.entity.WeeklySchedule;
import io.github.plaguewzk.qfnujavaapi.parser.impl.WeeklyScheduleParser;
import lombok.RequiredArgsConstructor;

/**
 * Created on 2026/1/2 23:16
 *
 * @author PlagueWZK
 */
@RequiredArgsConstructor
public class CourseService {
    private final QFNUExecutor qfnuExecutor;
    private final WeeklyScheduleParser weeklyScheduleParser = new WeeklyScheduleParser();

    public WeeklySchedule getCurrentWeeklySchedule() {
        return null;
    }
}
