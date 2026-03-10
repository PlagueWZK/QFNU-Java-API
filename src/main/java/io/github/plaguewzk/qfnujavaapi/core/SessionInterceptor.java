package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.exception.NetworkException;
import io.github.plaguewzk.qfnujavaapi.exception.SessionRefreshException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created on 2026/1/1 00:58
 *
 * @author PlagueWZK
 */

@SuppressWarnings("ClassCanBeRecord")
@Slf4j
public class SessionInterceptor implements Interceptor {
    private final Runnable loginAction;

    public SessionInterceptor(Runnable loginAction) {
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
                throw new SessionRefreshException("自动登录失败：登录动作未初始化");
            }
            synchronized (this) {
                try {
                    loginAction.run();
                    log.info("自动登录成功, 尝试重新发送请求...");
                    Request newRequest = request.newBuilder().build();
                    return chain.proceed(newRequest);
                } catch (QFNUAPIException e) {
                    throw new SessionRefreshException("尝试重新自动登录失败", e);
                } catch (Exception e) {
                    throw new SessionRefreshException("尝试重新自动登录时发生未知错误", e);
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
            if (preview.contains("请输入账号") && preview.contains("请输入密码") && preview.contains("请输入验证码")) return true;
        } catch (IOException e) {
            log.warn("检测 Session 过期时读取 Body 失败");
            throw new NetworkException("检测 Session 状态时读取响应体失败", e);
        }
        return false;
    }
}
