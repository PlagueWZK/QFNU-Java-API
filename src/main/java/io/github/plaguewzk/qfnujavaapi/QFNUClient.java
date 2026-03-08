package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.core.QFNUContext;
import io.github.plaguewzk.qfnujavaapi.core.QFNUCookieJar;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.core.ServiceFactory;
import io.github.plaguewzk.qfnujavaapi.core.SessionInterceptor;
import io.github.plaguewzk.qfnujavaapi.service.CaptchaService;
import io.github.plaguewzk.qfnujavaapi.service.LoginService;
import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.time.Duration;

/**
 * Created on 2025/12/30 01:13
 * 客户端入口 - 统一管理 Http 客户端和执行器
 *
 * @author PlagueWZK
 */
@Slf4j
public class QFNUClient {

    private static final int DEFAULT_LOGIN_RETRY_COUNT = 20;

    private final QFNUContext context;
    private final ServiceFactory serviceFactory;
    private final QFNUExecutor executor;

    private QFNUClient(String userAccount, String userPassword, CaptchaService captchaService) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(new QFNUCookieJar())
                .addInterceptor(new SessionInterceptor(this::login))
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .followRedirects(true)
                .build();
        this.executor = new QFNUExecutor(httpClient);
        this.context = new QFNUContext(this.executor, userAccount, userPassword, captchaService);
        this.serviceFactory = new ServiceFactory(this.context);
    }

    public QFNUExecutor executor() {
        return executor;
    }

    public QFNUContext context() {
        return context;
    }

    public <T> T service(Class<T> serviceType) {
        return serviceFactory.getService(serviceType);
    }

    private void login() {
        service(LoginService.class).autoLogin(DEFAULT_LOGIN_RETRY_COUNT);
    }

    public static class Builder {
        private String userAccount;
        private String userPassword;
        private CaptchaService captchaService;

        public Builder account(String account, String password) {
            if (account == null || account.isBlank()) {
                throw new InvalidParameterException("账号(account)不能为空");
            }
            if (password == null || password.isBlank()) {
                throw new InvalidParameterException("密码(password)不能为空");
            }
            this.userAccount = account;
            this.userPassword = password;
            return this;
        }

        public Builder captchaService(CaptchaService captchaService) {
            this.captchaService = captchaService;
            return this;
        }

        public QFNUClient build() {
            return new QFNUClient(userAccount, userPassword, captchaService);
        }
    }
}
