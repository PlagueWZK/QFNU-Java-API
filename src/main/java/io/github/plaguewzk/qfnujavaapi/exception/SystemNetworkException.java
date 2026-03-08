package io.github.plaguewzk.qfnujavaapi.exception;

public class SystemNetworkException extends QFNUAPIException {
    public SystemNetworkException() {
    }

    public SystemNetworkException(String message) {
        super(message);
    }

    public SystemNetworkException(Throwable cause) {
        super(cause);
    }

    public SystemNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
