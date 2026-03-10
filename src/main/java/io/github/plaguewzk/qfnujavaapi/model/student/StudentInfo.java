package io.github.plaguewzk.qfnujavaapi.model.student;

import java.io.Serializable;

public record StudentInfo(String name,
                          String studentId,
                          String academy,
                          String major,
                          String className) implements Serializable {
}
