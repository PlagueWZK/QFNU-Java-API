package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.exception.SystemNetworkException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
public record QFNUExecutor(OkHttpClient client) {

    public String executeGet(QFNUAPI endpoint) {
        Request request = new Request.Builder().url(endpoint.getValue()).get().build();
        return executeForString(request);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, String referer) {
        FormBody.Builder builder = new FormBody.Builder();
        body.forEach(builder::add);

        Request request = new Request.Builder()
                .url(endpoint.getValue())
                .post(builder.build())
                .header("Referer", referer != null ? referer : QFNUAPI.INDEX.getValue())
                .build();
        return executeForString(request);
    }

    public byte[] executeForBytes(Request request) {
        try (Response response = call(request)) {
            ResponseBody body = response.body();
            return body != null ? body.bytes() : new byte[0];
        } catch (IOException e) {
            log.error("转换字节数组失败[{}]: {}", request.url(), e.getMessage());
            throw new QFNUAPIException(e);
        }
    }

    public String executeForString(Request request) {
        try (Response response = call(request)) {
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            log.error("转换字符串失败[{}]: {}", request.url(), e.getMessage());
            throw new QFNUAPIException(e);
        }
    }

    private Response call(Request request) {
        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                response.close();
                throw new SystemNetworkException("教务系统响应异常: " + response.code());
            }

            log.debug("请求执行成功: [{}]", request.url());
            return response;
        } catch (IOException e) {
            log.error("网络请求异常[{}]: {}", request.url(), e.getMessage());
            throw new QFNUAPIException("网络连接超时或解析异常", e);
        }
    }
}
