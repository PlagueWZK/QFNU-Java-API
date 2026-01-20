package io.github.plaguewzk.qfnujavaapi.exception;

public class UnknownErrorException extends QFNUAPIException{
    public UnknownErrorException(String message) {
        super(message);
    }

    public UnknownErrorException(Throwable cause) {
        super(cause);
    }

    public UnknownErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownErrorException() {
    }
}
