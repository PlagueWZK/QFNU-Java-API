package io.github.plaguewzk.qfnujavaapi.exception;

/**
 * 网络请求、响应状态或响应体读取相关异常。
 */
public class NetworkException extends QFNUAPIException {
    public NetworkException() {
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
