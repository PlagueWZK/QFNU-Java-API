package io.github.plaguewzk.qfnujavaapi.service;

/**
 * Created on 2025/12/30 21:39
 *
 * @author PlagueWZK
 */

public interface CaptchaService {
    String recognize(byte[] imageBytes);
}
