package io.github.plaguewzk.qfnujavaapi.service.impl;

import io.github.plaguewzk.qfnujavaapi.core.QFNUAPI;
import io.github.plaguewzk.qfnujavaapi.core.QFNUCookieJar;
import io.github.plaguewzk.qfnujavaapi.core.QFNUExecutor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地评估工具：使用真实验证码和真实登录接口评估 OCR 策略命中率。
 * 运行时通过系统属性传入账号密码，避免把敏感信息写入主代码。
 */
public final class CaptchaStrategyEvaluatorApp {
    private static final int DEFAULT_SAMPLE_COUNT = 12;
    private static final Logger log = LoggerFactory.getLogger(CaptchaStrategyEvaluatorApp.class);

    private CaptchaStrategyEvaluatorApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        String account = args.length > 0 ? args[0] : requireProperty("captcha.eval.account");
        String password = args.length > 1 ? args[1] : requireProperty("captcha.eval.password");
        int sampleCount = args.length > 2 ? Integer.parseInt(args[2]) : Integer.getInteger("captcha.eval.samples", DEFAULT_SAMPLE_COUNT);

        DefaultCaptchaService.StrategySpec otsuPsm8 = DefaultCaptchaService.StrategySpec.otsu("otsu-psm8", 8);
        DefaultCaptchaService.StrategySpec otsuPsm7 = DefaultCaptchaService.StrategySpec.otsu("otsu-psm7", 7);
        DefaultCaptchaService.StrategySpec fixed145 = DefaultCaptchaService.StrategySpec.fixed("fixed-145-psm8", 8, 145);
        DefaultCaptchaService.StrategySpec fixed160 = DefaultCaptchaService.StrategySpec.fixed("fixed-160-psm8", 8, 160);
        DefaultCaptchaService.StrategySpec fixed170 = DefaultCaptchaService.StrategySpec.fixed("fixed-170-psm8", 8, 170);
        DefaultCaptchaService.StrategySpec contrast = DefaultCaptchaService.StrategySpec.contrast("contrast-psm7", 7);

        List<EvaluationTarget> targets = List.of(
                new EvaluationTarget("otsu-psm8", List.of(otsuPsm8)),
                new EvaluationTarget("otsu-psm7", List.of(otsuPsm7)),
                new EvaluationTarget("fixed-145-psm8", List.of(fixed145)),
                new EvaluationTarget("fixed-160-psm8", List.of(fixed160)),
                new EvaluationTarget("fixed-170-psm8", List.of(fixed170)),
                new EvaluationTarget("contrast-psm7", List.of(contrast)),
                new EvaluationTarget("combo-otsu+160", List.of(otsuPsm8, fixed160)),
                new EvaluationTarget("combo-otsu+170", List.of(otsuPsm8, fixed170))
        );

        List<StrategyStats> results = new ArrayList<>();
        for (EvaluationTarget target : targets) {
            DefaultCaptchaService service = new DefaultCaptchaService(target.specs());
            results.add(evaluateStrategy(service, target.name(), account, password, sampleCount));
        }

        results.sort(Comparator
                .comparingDouble(StrategyStats::successRate)
                .thenComparingDouble(StrategyStats::validRate)
                .reversed());

        System.out.println("真实验证码评估结束，样本数/策略: " + sampleCount);
        for (StrategyStats stats : results) {
            System.out.printf(
                    "%s -> success=%d/%d, valid=%d/%d, invalid=%d, captchaError=%d, unknown=%d%n",
                    stats.name(),
                    stats.successCount(),
                    stats.sampleCount(),
                    stats.validCount(),
                    stats.sampleCount(),
                    stats.invalidCount(),
                    stats.captchaErrorCount(),
                    stats.unknownCount()
            );
        }
    }

    private static StrategyStats evaluateStrategy(DefaultCaptchaService service,
                                                  String strategyName,
                                                  String account,
                                                  String password,
                                                  int sampleCount) throws InterruptedException {
        int successCount = 0;
        int validCount = 0;
        int invalidCount = 0;
        int captchaErrorCount = 0;
        int unknownCount = 0;

        for (int i = 0; i < sampleCount; i++) {
            AttemptResult result = singleAttempt(service, account, password);
            if (result.validCaptcha()) {
                validCount++;
            } else {
                invalidCount++;
            }
            switch (result.loginResult()) {
                case SUCCESS -> successCount++;
                case CAPTCHA_ERROR -> captchaErrorCount++;
                case UNKNOWN -> unknownCount++;
            }
            System.out.printf("策略[%s] 第 %d/%d 次: candidate=%s, valid=%s, result=%s%n",
                    strategyName, i + 1, sampleCount, result.candidate(), result.validCaptcha(), result.loginResult());
            TimeUnit.MILLISECONDS.sleep(150);
        }

        return new StrategyStats(strategyName, sampleCount, successCount, validCount, invalidCount, captchaErrorCount, unknownCount);
    }

    private static AttemptResult singleAttempt(DefaultCaptchaService service, String account, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new QFNUCookieJar())
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .followRedirects(true)
                .build();
        QFNUExecutor executor = new QFNUExecutor(client);

        Request captchaRequest = new Request.Builder()
                .url(QFNUAPI.CAPTCHA.value)
                .build();
        byte[] imageBytes = executor.executeForBytes(captchaRequest);
        String candidate;
        try {
            candidate = service.recognize(imageBytes);
        } catch (Exception e) {
            log.debug("识别阶段失败: {}", e.getMessage());
            return new AttemptResult("", false, LoginResult.UNKNOWN);
        }
        if (!isValidCandidate(candidate)) {
            return new AttemptResult(candidate, false, LoginResult.CAPTCHA_ERROR);
        }
        LoginResult loginResult = tryLogin(executor, account, password, candidate);
        if (loginResult == LoginResult.SUCCESS) {
            safeLogout(executor);
        }
        return new AttemptResult(candidate, isValidCandidate(candidate), loginResult);
    }

    private static LoginResult tryLogin(QFNUExecutor executor, String account, String password, String captcha) {
        String encoded = Base64.getEncoder().encodeToString(account.getBytes()) +
                "%%%" +
                Base64.getEncoder().encodeToString(password.getBytes());
        Map<String, String> formData = new HashMap<>();
        formData.put("userAccount", "");
        formData.put("userPassword", "");
        formData.put("RANDOMCODE", captcha);
        formData.put("encoded", encoded);

        String html = executor.executePost(QFNUAPI.LOGIN_POST, formData, QFNUAPI.INDEX);
        if (html.contains("验证码错误")) {
            return LoginResult.CAPTCHA_ERROR;
        }
        if (html.contains("密码错误") || html.contains("账号不存在")) {
            throw new IllegalStateException("传入的评估账号或密码无效");
        }
        return html.contains("退出") ? LoginResult.SUCCESS : LoginResult.UNKNOWN;
    }

    private static void safeLogout(QFNUExecutor executor) {
        try {
            executor.executeGet(QFNUAPI.LOGOUT_APP, Map.of(
                    "method", "exit",
                    "tktime", String.valueOf(System.currentTimeMillis())
            ));
            executor.executeGet(QFNUAPI.LOGOUT_CAS);
        } catch (Exception e) {
            log.debug("本地评估退出登录失败: {}", e.getMessage());
        }
    }

    private static boolean isValidCandidate(String candidate) {
        return candidate != null && candidate.matches("^[0-9a-z]{4}$");
    }

    private static String requireProperty(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("缺少系统属性: " + key);
        }
        return value;
    }

    private enum LoginResult {
        SUCCESS,
        CAPTCHA_ERROR,
        UNKNOWN
    }

    private static final class AttemptResult {
        private final String candidate;
        private final boolean validCaptcha;
        private final LoginResult loginResult;

        private AttemptResult(String candidate, boolean validCaptcha, LoginResult loginResult) {
            this.candidate = candidate;
            this.validCaptcha = validCaptcha;
            this.loginResult = loginResult;
        }

        private String candidate() {
            return candidate;
        }

        private boolean validCaptcha() {
            return validCaptcha;
        }

        private LoginResult loginResult() {
            return loginResult;
        }
    }

    private static final class EvaluationTarget {
        private final String name;
        private final List<DefaultCaptchaService.StrategySpec> specs;

        private EvaluationTarget(String name, List<DefaultCaptchaService.StrategySpec> specs) {
            this.name = name;
            this.specs = specs;
        }

        private String name() {
            return name;
        }

        private List<DefaultCaptchaService.StrategySpec> specs() {
            return specs;
        }
    }

    private static final class StrategyStats {
        private final String name;
        private final int sampleCount;
        private final int successCount;
        private final int validCount;
        private final int invalidCount;
        private final int captchaErrorCount;
        private final int unknownCount;

        private StrategyStats(String name, int sampleCount, int successCount, int validCount, int invalidCount, int captchaErrorCount, int unknownCount) {
            this.name = name;
            this.sampleCount = sampleCount;
            this.successCount = successCount;
            this.validCount = validCount;
            this.invalidCount = invalidCount;
            this.captchaErrorCount = captchaErrorCount;
            this.unknownCount = unknownCount;
        }

        private String name() {
            return name;
        }

        private int sampleCount() {
            return sampleCount;
        }

        private int successCount() {
            return successCount;
        }

        private int validCount() {
            return validCount;
        }

        private int invalidCount() {
            return invalidCount;
        }

        private int captchaErrorCount() {
            return captchaErrorCount;
        }

        private int unknownCount() {
            return unknownCount;
        }

        private double successRate() {
            return sampleCount == 0 ? 0D : (double) successCount / sampleCount;
        }

        private double validRate() {
            return sampleCount == 0 ? 0D : (double) validCount / sampleCount;
        }
    }
}
