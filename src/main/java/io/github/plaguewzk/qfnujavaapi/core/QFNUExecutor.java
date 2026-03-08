package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.exception.SystemChangedException;
import io.github.plaguewzk.qfnujavaapi.exception.SystemNetworkException;
import io.github.plaguewzk.qfnujavaapi.exception.UnknownErrorException;
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
        return URLEncoder.encode(Objects.requireNonNull(value), StandardCharsets.UTF_8);
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
            throw new SystemNetworkException("读取响应字节流失败: " + request.url(), e);
        }
    }

    public String executeForString(Request request) {
        try (Response response = call(request)) {
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            log.error("转换字符串失败[{}]: {}", request.url(), e.getMessage());
            throw new SystemNetworkException("读取响应文本失败: " + request.url(), e);
        }
    }

    public String buildUrl(QFNUAPI baseApi, Map<String, String> queryParameters) {
        HttpUrl parsed = HttpUrl.parse(baseApi.value);
        if (parsed == null) {
            throw new SystemChangedException("API 地址非法，无法构建请求: " + baseApi);
        }
        HttpUrl.Builder builder = parsed.newBuilder();
        if (queryParameters != null) {
            queryParameters.forEach(builder::addQueryParameter);
        }
        return builder.build().toString();
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
            throw new SystemNetworkException("请求教务系统失败: " + request.url(), e);
        } catch (QFNUAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new UnknownErrorException("执行请求时发生未知错误: " + request.url(), e);
        }
    }
}
