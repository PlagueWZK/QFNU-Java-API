package io.github.plaguewzk.qfnujavaapi.exception;

public class AccountOrPasswordErrorException extends AuthenticationException {
    public AccountOrPasswordErrorException(String message) {
        super(message);
    }

    public AccountOrPasswordErrorException(Throwable cause) {
        super(cause);
    }

    public AccountOrPasswordErrorException() {
    }

    public AccountOrPasswordErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
