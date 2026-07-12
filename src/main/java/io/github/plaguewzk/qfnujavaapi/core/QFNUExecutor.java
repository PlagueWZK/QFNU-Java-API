package io.github.plaguewzk.qfnujavaapi.core;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
import io.github.plaguewzk.qfnujavaapi.exception.NetworkException;
import io.github.plaguewzk.qfnujavaapi.exception.PageStructureException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

@Slf4j
public record QFNUExecutor(OkHttpClient client) {

    public String executeGet(QFNUAPI endpoint) {
        Request request = new Request.Builder().url(endpoint.value).get().build();
        return executeForString(request);
    }

    public String executeGet(QFNUAPI endpoint, Map<String, String> queryParameters) {
        Request request = new Request.Builder().url(buildUrl(endpoint, queryParameters)).get().build();
        return executeForString(request);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, String referer) {
        FormBody.Builder builder = new FormBody.Builder();
        body.forEach(builder::add);
        return executePostBody(endpoint, builder.build(), referer);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, QFNUAPI refererApi) {
        return executePost(endpoint, body, refererApi.value);
    }

    public String executePost(QFNUAPI endpoint, Map<String, String> body, QFNUAPI refererApi, Map<String, String> queryParameters) {
        String referer = buildUrl(refererApi, queryParameters);
        return executePost(endpoint, body, referer);
    }

    /**
     * 以 URL 编码字符串作为表单体执行 POST 请求（支持重复 key）。
     */
    public String executeFormPost(QFNUAPI endpoint, String urlEncodedBody, String referer) {
        RequestBody requestBody = RequestBody.create(
                urlEncodedBody,
                MediaType.parse("application/x-www-form-urlencoded; charset=utf-8")
        );
        return executePostBody(endpoint, requestBody, referer);
    }

    public String executeFormPost(QFNUAPI endpoint, String urlEncodedBody, QFNUAPI refererApi) {
        return executeFormPost(endpoint, urlEncodedBody, refererApi.value);
    }

    // ---- 内部方法 ----

    /**
     * 统一的 POST 请求执行，消除 executePost / executeFormPost 的重复代码。
     */
    private String executePostBody(QFNUAPI endpoint, RequestBody body, String referer) {
        Request request = new Request.Builder()
                .url(endpoint.value)
                .post(body)
                .header("Referer", referer != null ? referer : QFNUAPI.INDEX.value)
                .build();
        return executeForString(request);
    }

    public byte[] executeForBytes(Request request) {
        try (Response response = call(request)) {
            ResponseBody body = response.body();
            return body != null ? body.bytes() : new byte[0];
        } catch (IOException e) {
            log.error("转换字节数组失败[{}]: {}", request.url(), e.getMessage());
            throw new NetworkException("读取响应字节流失败: " + request.url(), e);
        }
    }

    public String executeForString(Request request) {
        try (Response response = call(request)) {
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            log.error("转换字符串失败[{}]: {}", request.url(), e.getMessage());
            throw new NetworkException("读取响应文本失败: " + request.url(), e);
        }
    }

    public String buildUrl(QFNUAPI baseApi, Map<String, String> queryParameters) {
        HttpUrl parsed = HttpUrl.parse(baseApi.value);
        if (parsed == null) {
            throw new PageStructureException("API 地址非法，无法构建请求: " + baseApi);
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
                throw new NetworkException("教务系统响应异常: " + response.code());
            }
            log.debug("请求执行成功: [{}]", request.url());
            return response;
        } catch (IOException e) {
            log.error("网络请求异常[{}]: {}", request.url(), e.getMessage());
            throw new NetworkException("请求教务系统失败: " + request.url(), e);
        } catch (QFNUAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new QFNUAPIException("执行请求时发生未知错误: " + request.url(), e);
        }
    }
}
