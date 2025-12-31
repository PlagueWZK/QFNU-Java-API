package io.github.plaguewzk.qfnujavaapi.model.entity;

import java.io.Serializable;

/**
 * Created on 2025/12/31 00:16
 *
 * @author PlagueWZK
 */

public record StudentInfo(
        String name,
        String studentId,
        String academy,
        String major,
        String className
) implements Serializable {
}
