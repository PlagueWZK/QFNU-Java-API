package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created on 2026/1/1 00:58
 *
 * @author PlagueWZK
 */

@SuppressWarnings("ClassCanBeRecord")
@Slf4j
public class SessionInterceptor implements Interceptor {
    private final Supplier<Boolean> loginAction;

    public SessionInterceptor(Supplier<Boolean> loginAction) {
        this.loginAction = loginAction;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (isSessionExpired(response, request)) {
            log.info("检测到未登录或Session过期, 执行自动登录");
            response.close();
            if (loginAction == null) {
                throw new QFNUAPIException("自动登录失败, 自动登录操作方法为null");
            }
            synchronized (this) {
                try {
                    boolean autoLoginSuccess = loginAction.get();
                    if (autoLoginSuccess) {
                        log.info("自动登录成功, 尝试重新发送请求...");
                        Request newRequest = request.newBuilder().build();
                        return chain.proceed(newRequest);
                    }
                } catch (Exception e) {
                    throw new QFNUAPIException("尝试重新自动登录失败", e);
                }
            }
        }
        return response;
    }

    private boolean isSessionExpired(Response response, Request originalRequest) {
        String currentUrl = response.request().url().toString();
        String originalUrl = originalRequest.url().toString();

        if (originalUrl.equals(QFNUAPI.INDEX.value) || originalUrl.contains(QFNUAPI.LOGIN_POST.value) || originalUrl.contains(QFNUAPI.CAPTCHA.value)) {
            return false;
        }
        if (originalUrl.contains("logout") || originalUrl.contains("method=exit")) {
            return false;
        }
        if (currentUrl.equals(QFNUAPI.INDEX.value)
                || currentUrl.contains("/jsxsd/xk/LoginToXk")
                || currentUrl.endsWith("/jsxsd/")) {
            return true;
        }
        try {
            String preview = response.peekBody(4096).string();
            if (preview.contains("登录")) return true;
        } catch (IOException e) {
            log.warn("检测 Session 过期时读取 Body 失败", e);
        }
        return false;
    }
}
