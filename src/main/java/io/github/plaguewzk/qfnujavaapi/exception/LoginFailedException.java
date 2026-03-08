package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 登录流程执行失败，但不一定是账号密码错误。
 */
public class LoginFailedException extends AuthenticationException {
    public LoginFailedException() {
    }

    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(Throwable cause) {
        super(cause);
    }

    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
