package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.LoginFailedException;
import io.github.plaguewzk.qfnujavaapi.exception.SessionRefreshException;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SessionInterceptorTest {

    private static Request protectedRequest() {
        return new Request.Builder()
                .url(QFNUAPI.MAIN_NEW_PAGE.value + "?t1=1")
                .build();
    }

    private static Response responseFor(Request request, String requestUrl, String body) {
        Request actualRequest = request.newBuilder().url(requestUrl).build();
        return new Response.Builder()
                .request(actualRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(body, null))
                .build();
    }

    @Test
    void shouldWrapLoginFailureAndPreserveCause() {
        LoginFailedException cause = new LoginFailedException("登录失败");
        SessionInterceptor interceptor = new SessionInterceptor(() -> {
            throw cause;
        });
        RecordingChain chain = new RecordingChain(protectedRequest(), List.of(
                responseFor(protectedRequest(), QFNUAPI.INDEX.value, "<html>请输入账号请输入密码请输入验证码</html>")
        ));

        SessionRefreshException exception = assertThrows(SessionRefreshException.class, () -> interceptor.intercept(chain));

        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldRetryOriginalRequestAfterLoginSuccess() throws IOException {
        SessionInterceptor interceptor = new SessionInterceptor(() -> {
        });
        Request request = protectedRequest();
        RecordingChain chain = new RecordingChain(request, List.of(
                responseFor(request, QFNUAPI.INDEX.value, "<html>请输入账号请输入密码请输入验证码</html>"),
                responseFor(request, request.url().toString(), "<html>ok</html>")
        ));

        Response response = interceptor.intercept(chain);

        assertEquals(2, chain.proceedCount());
        assertEquals(request.url().toString(), response.request().url().toString());
        response.close();
    }

    private static final class RecordingChain implements Interceptor.Chain {
        private final Request request;
        private final List<Response> responses;
        private int proceedCount;

        private RecordingChain(Request request, List<Response> responses) {
            this.request = request;
            this.responses = new ArrayList<>(responses);
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public Response proceed(Request request) {
            Response response = responses.get(proceedCount);
            proceedCount++;
            return response;
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public Call call() {
            return null;
        }

        @Override
        public int connectTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int readTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int writeTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        private int proceedCount() {
            return proceedCount;
        }
    }
}
