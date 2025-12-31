package io.github.plaguewzk.qfnujavaapi.exception;

public class SystemChangedException extends QFNUAPIException {
    public SystemChangedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemChangedException() {
    }

    public SystemChangedException(String message) {
        super(message);
    }

    public SystemChangedException(Throwable cause) {
        super(cause);
    }
}
