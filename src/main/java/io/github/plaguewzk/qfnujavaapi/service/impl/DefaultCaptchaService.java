package io.github.plaguewzk.qfnujavaapi.service.impl;

import io.github.plaguewzk.qfnujavaapi.exception.QFNUAPIException;
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

/**
 * Created on 2025/12/30 01:11
 * Optimized for Higher Accuracy
 *
 * @author PlagueWZK
 */
@SuppressWarnings("SpellCheckingInspection")
@Slf4j
public class DefaultCaptchaService implements CaptchaService {
    private static final String TEMP_DIR_NAME = "qfnu_api_tessdata";
    private static final String DATA_FILE_NAME = "eng.traineddata";
    private final ITesseract tesseract;

    public DefaultCaptchaService() {
        this.tesseract = new Tesseract();
        String dataPath = loadAndReleaseTessData();
        log.debug("默认OCR: Tesseract 数据路径设置为: {}", dataPath);
        this.tesseract.setDatapath(dataPath);
        this.tesseract.setLanguage("eng");

        this.tesseract.setVariable("tessedit_pageseg_mode", "7");

        this.tesseract.setVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz");
        this.tesseract.setVariable("load_system_dawg", "F");
        this.tesseract.setVariable("load_freq_dawg", "F");
    }

    private String loadAndReleaseTessData() {
        try {
            String systemTemp = System.getProperty("java.io.tmpdir");
            Path dirPath = Paths.get(systemTemp, TEMP_DIR_NAME);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(DATA_FILE_NAME);

            if (Files.notExists(filePath) || Files.size(filePath) == 0) {
                log.info("正在释放训练数据到临时目录: {}", filePath.toAbsolutePath());
                try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("tessdata/" + DATA_FILE_NAME)) {
                    if (in == null) {
                        throw new QFNUAPIException("严重错误：Jar包内找不到 /tessdata/" + DATA_FILE_NAME + " 文件！");
                    }
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return dirPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("无法初始化 OCR 引擎，操作路径失败", e);
        }
    }

    @Override
    public String recognize(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) return "";
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);
            if (image == null) return "";
            BufferedImage processedImage = preprocessPipeline(image);
            synchronized (this) {
                String result = tesseract.doOCR(processedImage);
                return result.replaceAll("\\s+", "").trim();
            }
        } catch (TesseractException | IOException e) {
            log.error("验证码识别报错: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 整合后的图像预处理流水线
     */
    private BufferedImage preprocessPipeline(BufferedImage original) {
        BufferedImage scaled = scaleImage(original, 3.0);
        BufferedImage binary = convertToBinaryOtsu(scaled);
        return removeNoise(binary);
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
}
