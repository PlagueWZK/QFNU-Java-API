package io.github.plaguewzk.qfnujavaapi.service.impl;

import io.github.plaguewzk.qfnujavaapi.exception.CaptchaInitializationException;
import io.github.plaguewzk.qfnujavaapi.exception.CaptchaRecognitionException;
import io.github.plaguewzk.qfnujavaapi.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created on 2025/12/30 01:11
 * Optimized for Higher Accuracy
 *
 * @author PlagueWZK
 */
@Slf4j
public class DefaultCaptchaService implements CaptchaService {
    private static final String TEMP_DIR_NAME = "qfnu_api_tessdata";
    private static final String DATA_FILE_NAME = "eng.traineddata";
    private static final int CAPTCHA_LENGTH = 4;
    private static final Pattern CAPTCHA_PATTERN = Pattern.compile("^[0-9a-z]{4}$");
    private static final double DEFAULT_SCALE_FACTOR = 3.0;

    private final List<OcrStrategy> strategies;

    public DefaultCaptchaService() {
        this(defaultStrategySpecs());
    }

    DefaultCaptchaService(List<StrategySpec> strategySpecs) {
        String dataPath = loadAndReleaseTessData();
        log.debug("默认OCR: Tesseract 数据路径设置为: {}", dataPath);
        this.strategies = buildStrategies(dataPath, strategySpecs);
    }

    static List<StrategySpec> defaultStrategySpecs() {
        // 基于真实验证码评估，fixed-170-psm8 目前是默认主策略。
        return List.of(
                StrategySpec.fixed("fixed-170-psm8", 8, 170)
        );
    }

    private List<OcrStrategy> buildStrategies(String dataPath, List<StrategySpec> strategySpecs) {
        return strategySpecs.stream()
                .map(spec -> new OcrStrategy(spec, newTesseract(dataPath, spec.pageSegMode())))
                .toList();
    }

    private Tesseract newTesseract(String dataPath, int pageSegMode) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(dataPath);
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(pageSegMode);
        tesseract.setVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz");
        tesseract.setVariable("load_system_dawg", "F");
        tesseract.setVariable("load_freq_dawg", "F");
        tesseract.setVariable("user_defined_dpi", "300");
        return tesseract;
    }

    private String loadAndReleaseTessData() {
        try {
            String systemTemp = System.getProperty("java.io.tmpdir");
            Path dirPath = Paths.get(systemTemp, TEMP_DIR_NAME);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(DATA_FILE_NAME);

            if (Files.notExists(filePath) || Files.size(filePath) == 0) {
                log.info("OCR: 正在释放训练数据到临时目录: {}", filePath.toAbsolutePath());
                try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("tessdata/" + DATA_FILE_NAME)) {
                    if (in == null) {
                        throw new CaptchaInitializationException("验证码识别引擎初始化失败：Jar 包内缺少 tessdata/" + DATA_FILE_NAME);
                    }
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return dirPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new CaptchaInitializationException("验证码识别引擎初始化失败：无法释放训练数据", e);
        }
    }

    @Override
    public String recognize(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) return "";
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new CaptchaRecognitionException("验证码识别失败：图片内容为空");
            }
            return recognizeWithStrategies(image);
        } catch (TesseractException | IOException e) {
            log.error("验证码识别报错: {}", e.getMessage());
            throw new CaptchaRecognitionException("验证码识别失败", e);
        }
    }

    private String recognizeWithStrategies(BufferedImage original) throws TesseractException {
        for (OcrStrategy strategy : strategies) {
            String candidate;
            try {
                candidate = doRecognize(strategy, original);
            } catch (TesseractException e) {
                log.debug("OCR策略[{}]执行失败: {}", strategy.spec().name(), e.getMessage());
                continue;
            }
            if (!isValidCaptcha(candidate)) {
                log.debug("OCR策略[{}]结果无效: {}", strategy.spec().name(), candidate);
                continue;
            }
            log.debug("OCR策略[{}]得到有效结果: {}", strategy.spec().name(), candidate);
            return candidate;
        }
        throw new CaptchaRecognitionException("验证码识别失败：所有策略均未识别出合法的4位验证码");
    }

    private String doRecognize(OcrStrategy strategy, BufferedImage original) throws TesseractException {
        BufferedImage processedImage = preprocess(original, strategy.spec());
        synchronized (strategy.engine()) {
            return normalizeResult(strategy.engine().doOCR(processedImage));
        }
    }

    private String normalizeResult(String rawResult) {
        if (rawResult == null) {
            return "";
        }
        return rawResult
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z]", "")
                .trim();
    }

    private boolean isValidCaptcha(String candidate) {
        return candidate != null
                && candidate.length() == CAPTCHA_LENGTH
                && CAPTCHA_PATTERN.matcher(candidate).matches();
    }

    private BufferedImage preprocess(BufferedImage original, StrategySpec spec) {
        BufferedImage scaled = scaleImage(original, spec.scaleFactor());
        return switch (spec.pipeline()) {
            case OTSU -> removeNoise(convertToBinaryOtsu(scaled));
            case FIXED_THRESHOLD -> removeNoise(convertToBinaryFixedThreshold(scaled, spec.fixedThreshold()));
            case CONTRAST -> normalizeContrast(scaled);
        };
    }

    /**
     * 【改进3】图像放大
     */
    private BufferedImage scaleImage(BufferedImage image, double scaleFactor) {
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaledImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaledImage;
    }

    /**
     * 【改进4】使用 Otsu 算法进行自适应二值化
     * 自动计算最佳阈值，而不是死板的 if gray < 100
     */
    private BufferedImage convertToBinaryOtsu(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                pixels[y * width + x] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        int threshold = otsuThreshold(pixels);
        log.debug("Otsu 算法计算的动态阈值为: {}", threshold);
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = pixels[y * width + x];
                if (gray < threshold) {
                    binaryImage.setRGB(x, y, 0x000000);
                } else {
                    binaryImage.setRGB(x, y, 0xFFFFFF);
                }
            }
        }
        return binaryImage;
    }

    private BufferedImage convertToBinaryFixedThreshold(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                binaryImage.setRGB(x, y, gray < threshold ? 0x000000 : 0xFFFFFF);
            }
        }
        return binaryImage;
    }

    /**
     * Otsu 算法实现：寻找类间方差最大的阈值
     */
    private int otsuThreshold(int[] pixels) {
        int[] histogram = new int[256];
        for (int pixel : pixels) {
            histogram[pixel]++;
        }

        int total = pixels.length;
        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF;
        float varMax = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;

            sumB += (float) (t * histogram[t]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        return threshold;
    }

    private BufferedImage normalizeContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage normalized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int min = 255;
        int max = 0;
        int[] grays = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                grays[y * width + x] = gray;
                min = Math.min(min, gray);
                max = Math.max(max, gray);
            }
        }

        if (max == min) {
            return image;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grays[y * width + x];
                int normalizedGray = (gray - min) * 255 / (max - min);
                int rgb = (normalizedGray << 16) | (normalizedGray << 8) | normalizedGray;
                normalized.setRGB(x, y, rgb);
            }
        }
        return normalized;
    }

    private BufferedImage removeNoise(BufferedImage image) {
        // 但原本的 8 邻域少于 3 个黑点就清除的逻辑依然比较稳健，可以暂不修改。
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (image.getRGB(x, y) == 0xFF000000) {
                    int count = 0;
                    if (image.getRGB(x - 1, y - 1) == 0xFF000000) count++;
                    if (image.getRGB(x, y - 1) == 0xFF000000) count++;
                    if (image.getRGB(x + 1, y - 1) == 0xFF000000) count++;
                    if (image.getRGB(x - 1, y) == 0xFF000000) count++;
                    if (image.getRGB(x + 1, y) == 0xFF000000) count++;
                    if (image.getRGB(x - 1, y + 1) == 0xFF000000) count++;
                    if (image.getRGB(x, y + 1) == 0xFF000000) count++;
                    if (image.getRGB(x + 1, y + 1) == 0xFF000000) count++;
                    if (count < 2) { // 稍微保守一点，放大后噪点可能也会变大
                        image.setRGB(x, y, 0xFFFFFFFF);
                    }
                }
            }
        }
        return image;
    }

    enum Pipeline {
        OTSU,
        FIXED_THRESHOLD,
        CONTRAST
    }

    record StrategySpec(String name, int pageSegMode, Pipeline pipeline, int fixedThreshold, double scaleFactor) {
        static StrategySpec otsu(String name, int pageSegMode) {
            return new StrategySpec(name, pageSegMode, Pipeline.OTSU, -1, DEFAULT_SCALE_FACTOR);
        }

        static StrategySpec fixed(String name, int pageSegMode, int fixedThreshold) {
            return new StrategySpec(name, pageSegMode, Pipeline.FIXED_THRESHOLD, fixedThreshold, DEFAULT_SCALE_FACTOR);
        }

        static StrategySpec contrast(String name, int pageSegMode) {
            return new StrategySpec(name, pageSegMode, Pipeline.CONTRAST, -1, 4.0);
        }
    }

    private record OcrStrategy(StrategySpec spec, ITesseract engine) {
    }
}
