package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginServiceTest {

    @Test
    void shouldRejectMissingAccountInContext() {
        LoginService loginService = new LoginService(new QFNUContext(
                new QFNUExecutor(new OkHttpClient()),
                null,
                "password",
                null
        ));

        assertThrows(InvalidParameterException.class, () -> loginService.autoLogin(1));
    }

    @Test
    void shouldRejectMissingPasswordInContext() {
        LoginService loginService = new LoginService(new QFNUContext(
                new QFNUExecutor(new OkHttpClient()),
                "student",
                " ",
                null
        ));

        assertThrows(InvalidParameterException.class, () -> loginService.autoLogin(1));
    }
}
