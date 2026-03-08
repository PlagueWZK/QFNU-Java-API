package io.github.plaguewzk.qfnujavaapi.exception;

public class ParsingErrorException extends QFNUAPIException {
    public ParsingErrorException(String message) {
        super(message);
    }

    public ParsingErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingErrorException(Throwable cause) {
        super(cause);
    }

    public ParsingErrorException() {
    }
}
