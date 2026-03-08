package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 服务工厂创建服务实例失败。
 */
public class ServiceCreationException extends QFNUAPIException {
    public ServiceCreationException() {
    }

    public ServiceCreationException(String message) {
        super(message);
    }

    public ServiceCreationException(Throwable cause) {
        super(cause);
    }

    public ServiceCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
