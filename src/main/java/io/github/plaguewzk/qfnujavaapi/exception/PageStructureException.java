package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 页面结构变化导致当前解析逻辑无法继续工作。
 */
public class PageStructureException extends ParseException {
    public PageStructureException() {
    }

    public PageStructureException(String message) {
        super(message);
    }

    public PageStructureException(Throwable cause) {
        super(cause);
    }

    public PageStructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
