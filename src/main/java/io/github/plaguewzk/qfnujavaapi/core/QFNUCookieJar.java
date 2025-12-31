package io.github.plaguewzk.qfnujavaapi.core;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created on 2025/12/30 01:06
 *
 * @author PlagueWZK
 */
@Slf4j
public class QFNUCookieJar implements CookieJar {
    private final ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        String host = httpUrl.host();
        List<Cookie> cookies = cookieStore.getOrDefault(host, new ArrayList<>());
        return cookies.stream()
                .filter(cookie -> cookie.matches(httpUrl))
                .filter(cookie -> cookie.expiresAt() > System.currentTimeMillis())
                .collect(Collectors.toList());
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        String host = httpUrl.host();
        List<Cookie> cookies = cookieStore.computeIfAbsent(host, k -> new ArrayList<>());
        synchronized (cookies) {
            for (Cookie newCookie : list) {
                cookies.removeIf(current ->
                        current.name().equals(newCookie.name()) &&
                                current.domain().equals(newCookie.domain()) &&
                                current.path().equals(newCookie.path()));
                cookies.add(newCookie);
                LocalDateTime expiresTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(newCookie.expiresAt()), ZoneId.systemDefault());
                log.debug("更新Cookie [{}] [Path:{}] {} -> {} [expiresAt:{}, TimeRemaining:{}]", host, newCookie.path(), newCookie.name(), newCookie.value(), expiresTime, Duration.between(LocalDateTime.now(), expiresTime).toMinutes());
            }
        }
    }
}
