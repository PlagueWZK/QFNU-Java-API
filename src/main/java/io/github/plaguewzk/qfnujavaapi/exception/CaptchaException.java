package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 验证码能力相关异常的统一基类。
 */
public class CaptchaException extends QFNUAPIException {
    public CaptchaException() {
    }

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(Throwable cause) {
        super(cause);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
