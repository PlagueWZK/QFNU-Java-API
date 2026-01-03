package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.exception.SystemNetworkException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public record QFNUExecutor(OkHttpClient client) {

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new QFNUAPIException("URL编码失败:", e);
        }
    }

    public String executeGet(QFNUAPI endpoint) {
        Request request = new Request.Builder().url(endpoint.value).get().build();
        return executeForString(request);
    }

    public String executeGet(QFNUAPI endpoint, Map<String, String> queryParameters) {
        String queryString = queryParameters.entrySet().stream()
                .map((entry) -> String.join("=", encode(entry.getKey()), encode(entry.getValue())))
                .collect(Collectors.joining("&"));
        Request request = new Request.Builder().url(endpoint.value + "?" + queryString).get().build();
        return executeForString(request);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, String referer) {
        FormBody.Builder builder = new FormBody.Builder();
        body.forEach(builder::add);

        Request request = new Request.Builder()
                .url(endpoint.value)
                .post(builder.build())
                .header("Referer", referer != null ? referer : QFNUAPI.INDEX.value)
                .build();
        return executeForString(request);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, QFNUAPI refererApi) {
        return executePost(endpoint, body, refererApi.value);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, QFNUAPI refererApi, Map<String, String> queryParameters) {
        String referer = buildUrl(refererApi, queryParameters);
        return executePost(endpoint, body, referer);
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

    public String buildUrl(QFNUAPI baseApi, Map<String, String> queryParameters) {
        try {
            HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(baseApi.value)).newBuilder();
            if (queryParameters != null) {
                queryParameters.forEach(builder::addQueryParameter);
            }
            return builder.build().toString();
        } catch (Exception e) {
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
