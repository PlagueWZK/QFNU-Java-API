package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.service.CaptchaService;

import java.util.Objects;

/**
 * 共享运行时上下文，集中保存服务需要复用的依赖与凭据。
 *
 * @author PlagueWZK
 */
public final class QFNUContext {
    private final QFNUExecutor executor;
    private final String userAccount;
    private final String userPassword;
    private final CaptchaService captchaService;

    public QFNUContext(QFNUExecutor executor, String userAccount, String userPassword, CaptchaService captchaService) {
        this.executor = Objects.requireNonNull(executor, "executor 不能为 null");
        this.userAccount = userAccount;
        this.userPassword = userPassword;
        this.captchaService = captchaService;
    }

    public QFNUExecutor executor() {
        return executor;
    }

    public String userAccount() {
        return userAccount;
    }

    public String userPassword() {
        return userPassword;
    }

    public CaptchaService captchaService() {
        return captchaService;
    }
}
