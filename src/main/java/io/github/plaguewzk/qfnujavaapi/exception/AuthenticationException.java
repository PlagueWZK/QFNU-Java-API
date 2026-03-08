package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 认证与会话相关异常的统一基类。
 */
public class AuthenticationException extends QFNUAPIException {
    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
