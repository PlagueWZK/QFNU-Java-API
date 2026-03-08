package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 验证码识别引擎初始化失败。
 */
public class CaptchaInitializationException extends CaptchaException {
    public CaptchaInitializationException() {
    }

    public CaptchaInitializationException(String message) {
        super(message);
    }

    public CaptchaInitializationException(Throwable cause) {
        super(cause);
    }

    public CaptchaInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
