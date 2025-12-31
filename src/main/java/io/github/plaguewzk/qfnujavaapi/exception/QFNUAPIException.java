package io.github.plaguewzk.qfnujavaapi.exception;

public class QFNUAPIException extends RuntimeException {
    public QFNUAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public QFNUAPIException() {
    }

    public QFNUAPIException(String message) {
        super(message);
    }

    public QFNUAPIException(Throwable cause) {
        super(cause);
    }
}
