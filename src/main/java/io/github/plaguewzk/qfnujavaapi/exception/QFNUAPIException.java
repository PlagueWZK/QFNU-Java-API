package io.github.plaguewzk.qfnujavaapi.exception;

public class QFNUAPIException extends RuntimeException {
    public QFNUAPIException() {
    }

    public QFNUAPIException(String message) {
        super(message);
    }

    public QFNUAPIException(Throwable cause) {
        super(cause);
    }

    public QFNUAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
