package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 页面结构仍可访问，但具体字段格式或内容不符合预期。
 */
public class ParsingErrorException extends ParseException {
    public ParsingErrorException() {
    }

    public ParsingErrorException(String message) {
        super(message);
    }

    public ParsingErrorException(Throwable cause) {
        super(cause);
    }

    public ParsingErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
