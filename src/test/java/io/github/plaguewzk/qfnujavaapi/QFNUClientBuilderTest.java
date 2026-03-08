package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QFNUClientBuilderTest {

    @Test
    void shouldRejectBlankAccount() {
        QFNUClient.Builder builder = new QFNUClient.Builder();

        assertThrows(InvalidParameterException.class, () -> builder.account(" ", "password"));
    }

    @Test
    void shouldRejectBlankPassword() {
        QFNUClient.Builder builder = new QFNUClient.Builder();

        assertThrows(InvalidParameterException.class, () -> builder.account("student", " "));
    }
}
