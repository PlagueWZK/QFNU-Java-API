package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 页面解析相关异常统一基类。
 */
public class ParseException extends QFNUAPIException {
    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
