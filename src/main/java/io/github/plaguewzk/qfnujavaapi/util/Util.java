package io.github.plaguewzk.qfnujavaapi.util;

import java.time.format.DateTimeFormatter;

/**
 * Created on 2026/1/2 15:42
 *
 * @author PlagueWZK
 */

public class Util {
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER_IGNORE_SECOND = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    private Util() {
    }
}
