package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.core.QFNUCookieJar;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.core.SessionInterceptor;
import io.github.plaguewzk.qfnujavaapi.service.LoginService;
import io.github.plaguewzk.qfnujavaapi.service.StudentService;
import lombok.Getter;
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

    @Getter
    private final OkHttpClient httpClient;
    private final QFNUExecutor executor;
    private final String userAccount;
    private final String userPassword;
    private final LoginService loginService;
    private final StudentService studentService;


    private QFNUClient(String userAccount, String userPassword) {
        this.userAccount = userAccount;
        this.userPassword = userPassword;
        this.httpClient = new OkHttpClient.Builder()
                .cookieJar(new QFNUCookieJar())
                .addInterceptor(new SessionInterceptor(this::login))
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .followRedirects(true)
                .build();
        this.executor = new QFNUExecutor(this.httpClient);
        this.loginService = new LoginService(this.executor);
        this.studentService = new StudentService(this.executor);
    }

    public StudentService student() {
        return studentService;
    }

    public LoginService loginService() {
        return loginService;
    }

    public QFNUExecutor executor() {
        return executor;
    }

    private boolean login() {
        if (userAccount == null || userPassword == null) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }
        try {
            loginService.autoLogin(this.userAccount, this.userPassword, 20);
            return true;
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return false;
        }
    }

    public static class Builder {
        private String userAccount;
        private String userPassword;

        public Builder account(String account, String password) {
            this.userAccount = account;
            this.userPassword = password;
            return this;
        }

        public QFNUClient build() {
            return new QFNUClient(userAccount, userPassword);
        }
    }
}
