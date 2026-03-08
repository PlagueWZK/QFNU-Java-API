package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 会话失效后自动续期失败。
 */
public class SessionRefreshException extends AuthenticationException {
    public SessionRefreshException() {
    }

    public SessionRefreshException(String message) {
        super(message);
    }

    public SessionRefreshException(Throwable cause) {
        super(cause);
    }

    public SessionRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
