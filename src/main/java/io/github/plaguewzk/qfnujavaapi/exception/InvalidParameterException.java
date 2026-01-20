package io.github.plaguewzk.qfnujavaapi.exception;

public class InvalidParameterException extends QFNUAPIException {
    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(Throwable cause) {
        super(cause);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException() {
    }
}
