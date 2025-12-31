package io.github.plaguewzk.qfnujavaapi.service;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import io.github.plaguewzk.qfnujavaapi.exception.AccountOrPasswordErrorException;
import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.service.impl.DefaultCaptchaService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2025/12/30 01:09
 * 登录服务 - 负责处理登录逻辑、验证码识别及会话维持
 *
 * @author PlagueWZK
 */
@SuppressWarnings("ClassCanBeRecord")
@Slf4j
public class LoginService {
    private final QFNUExecutor executor;
    private final CaptchaService captchaService;

    public LoginService(QFNUExecutor executor, CaptchaService captchaService) {
        captchaService = captchaService != null ? captchaService : DefaultHolder.INSTANCE;
        this.executor = executor;
        this.captchaService = captchaService;
    }

    public LoginService(QFNUExecutor executor) {
        this(executor, null);
    }

    /**
     * 执行自动登录流程
     */
    public void autoLogin(String userAccount, String userPassword, int repeatCount) {
        int count = 0;
        while (count < repeatCount) {
            try {
                Request captchaReq = new Request.Builder()
                        .url(QFNUAPI.CAPTCHA.getValue())
                        .build();
                byte[] imgBytes = executor.executeForBytes(captchaReq);
                String code = captchaService.recognize(imgBytes);
                log.debug("第 {} 次尝试，账号: {}, 识别验证码: {}", count + 1, userAccount, code);
                if (performLoginRequest(userAccount, userPassword, code)) {
                    log.info("用户: {} 登录成功", userAccount);
                    return;
                }
                log.warn("第 {} 次登录失败，准备重试...", count + 1);
                count++;
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new QFNUAPIException("登录线程被中断", e);
            } catch (QFNUAPIException e) {
                if (e.getMessage().contains("账号或密码错误")) {
                    throw e;
                }
                log.error("登录过程出错: {}", e.getMessage());
                count++;
            } catch (Exception e) {
                log.error("未知异常: {}", e.getMessage());
                count++;
            }
        }
        throw new QFNUAPIException("登录失败：达到最大重试次数，请检查网络或验证码服务");
    }

    /**
     * 模拟一次登录请求
     *
     * @param userAccount  学号
     * @param userPassword 密码
     * @param captcha      验证码
     * @return 如果登录成功，返回true; 如果验证码错误或登录失败，返回false; 如果账号或密码错误,抛出{@link AccountOrPasswordErrorException}
     */
    private boolean performLoginRequest(String userAccount, String userPassword, String captcha) {
        String encoded = Base64.getEncoder().encodeToString(userAccount.getBytes()) +
                "%%%" +
                Base64.getEncoder().encodeToString(userPassword.getBytes());
        Map<String, String> formData = new HashMap<>();
        formData.put("userAccount", "");
        formData.put("userPassword", "");
        formData.put("RANDOMCODE", captcha);
        formData.put("encoded", encoded);
        String html = executor.executePost(QFNUAPI.LOGIN_POST, formData, QFNUAPI.INDEX.getValue());
        if (html.contains("验证码错误")) {
            log.warn("服务端返回：验证码错误");
            return false;
        }
        if (html.contains("密码错误") || html.contains("账号不存在")) {
            log.error("服务端返回：账号或密码错误");
            throw new AccountOrPasswordErrorException("账号或者密码错误");
        }
        return html.contains("退出");
    }

    private static class DefaultHolder {
        private static final CaptchaService INSTANCE = new DefaultCaptchaService();
    }
}
