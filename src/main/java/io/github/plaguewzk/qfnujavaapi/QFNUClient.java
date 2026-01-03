package io.github.plaguewzk.qfnujavaapi;

import io.github.plaguewzk.qfnujavaapi.core.QFNUCookieJar;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.core.SessionInterceptor;
import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.service.CourseService;
import io.github.plaguewzk.qfnujavaapi.service.LoginService;
import io.github.plaguewzk.qfnujavaapi.service.NotificationService;
import io.github.plaguewzk.qfnujavaapi.service.StudentService;
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

    private final QFNUExecutor executor;
    private final String userAccount;
    private final String userPassword;
    private final LoginService loginService;
    private final StudentService studentService;
    private final NotificationService notificationService;
    private final CourseService courseService;

    private QFNUClient(String userAccount, String userPassword) {
        this.userAccount = userAccount;
        this.userPassword = userPassword;
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(new QFNUCookieJar())
                .addInterceptor(new SessionInterceptor(this::login))
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .followRedirects(true)
                .build();
        this.executor = new QFNUExecutor(httpClient);
        this.loginService = new LoginService(this.executor);
        this.studentService = new StudentService(this.executor);
        this.notificationService = new NotificationService(this.executor);
        this.courseService = new CourseService(this.executor);
    }

    public StudentService student() {
        return studentService;
    }

    public LoginService loginService() {
        return loginService;
    }

    public NotificationService notificationService() {
        return notificationService;
    }

    public QFNUExecutor executor() {
        return executor;
    }

    public CourseService courseService() {
        return courseService;
    }

    private boolean login() {
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
            if (account == null || account.isBlank()) {
                throw new QFNUAPIException("账号(account)不能为null");
            }
            if (password == null || password.isBlank()) {
                throw new QFNUAPIException("密码(password)不能为null");
            }
            this.userAccount = account;
            this.userPassword = password;
            return this;
        }

        public QFNUClient build() {
            return new QFNUClient(userAccount, userPassword);
        }
    }
}
