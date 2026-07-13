package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.core.*;
import io.github.plaguewzk.qfnujavaapi.service.CaptchaService;
import io.github.plaguewzk.qfnujavaapi.service.LoginService;
import io.github.plaguewzk.qfnujavaapi.exception.InvalidParameterException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.time.Duration;
import java.util.List;

/**
 * Created on 2025/12/30 01:13
 * 客户端入口 - 统一管理 Http 客户端和执行器
 *
 * @author PlagueWZK
 */
@Slf4j
public class QFNUClient {

    private static final int DEFAULT_LOGIN_RETRY_COUNT = 10;
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);

    private final QFNUContext context;
    private final ComponentResolver resolver;
    private final QFNUExecutor executor;

    private QFNUClient(
            String userAccount, String userPassword, CaptchaService captchaService,
            List<QFNUModule> modules
    ) {
        this(userAccount, userPassword, captchaService, modules, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    private QFNUClient(
            String userAccount, String userPassword, CaptchaService captchaService, List<QFNUModule> modules,
            Duration connectTimeout, Duration readTimeout
    ) {
        OkHttpClient httpClient = new OkHttpClient.Builder().cookieJar(new QFNUCookieJar()).addInterceptor(
                new SessionInterceptor(this::login)).connectTimeout(connectTimeout).readTimeout(
                readTimeout).followRedirects(true).build();
        this.executor = new QFNUExecutor(httpClient);
        this.context = new QFNUContext(this.executor, userAccount, userPassword, captchaService);
        ComponentRegistry registry = new ComponentRegistry();
        new QFNUBuiltinModule().configure(registry, registry);
        for (QFNUModule module : modules) {
            module.configure(registry, registry);
        }
        this.resolver = new DefaultComponentResolver(
                this.context, registry.parserProviders(), registry.serviceProviders());
    }

    public QFNUExecutor executor() {
        return executor;
    }

    public QFNUContext context() {
        return context;
    }

    public <T> T service(Class<T> serviceType) {
        return resolver.service(serviceType);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void login() {
        service(LoginService.class).autoLogin(DEFAULT_LOGIN_RETRY_COUNT);
    }

    public static class Builder {
        private String userAccount;
        private String userPassword;
        private CaptchaService captchaService;
        private final List<QFNUModule> modules = new ArrayList<>();
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration readTimeout = DEFAULT_READ_TIMEOUT;

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

        public Builder install(QFNUModule module) {
            if (module == null) {
                throw new InvalidParameterException("扩展模块(module)不能为空");
            }
            this.modules.add(module);
            return this;
        }

        /**
         * 设置 HTTP 连接超时和读取超时。
         *
         * @param connectTimeout 连接超时，不能为 null，必须大于零
         * @param readTimeout    读取超时，不能为 null，必须大于零
         */
        public Builder timeout(Duration connectTimeout, Duration readTimeout) {
            if (connectTimeout == null) {
                throw new InvalidParameterException("连接超时(connectTimeout)不能为 null");
            }
            if (connectTimeout.isNegative() || connectTimeout.isZero()) {
                throw new InvalidParameterException("连接超时(connectTimeout)必须大于零");
            }
            if (readTimeout == null) {
                throw new InvalidParameterException("读取超时(readTimeout)不能为 null");
            }
            if (readTimeout.isNegative() || readTimeout.isZero()) {
                throw new InvalidParameterException("读取超时(readTimeout)必须大于零");
            }
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
            return this;
        }

        public QFNUClient build() {
            return new QFNUClient(
                    userAccount, userPassword, captchaService, List.copyOf(modules), connectTimeout,
                    readTimeout
            );
        }
    }
}
