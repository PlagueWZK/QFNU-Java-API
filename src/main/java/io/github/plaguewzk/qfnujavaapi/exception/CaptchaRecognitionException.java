package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 验证码识别过程执行失败。
 */
public class CaptchaRecognitionException extends CaptchaException {
    public CaptchaRecognitionException() {
    }

    public CaptchaRecognitionException(String message) {
        super(message);
    }

    public CaptchaRecognitionException(Throwable cause) {
        super(cause);
    }

    public CaptchaRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
