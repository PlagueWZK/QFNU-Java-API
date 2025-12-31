package io.github.plaguewzk.qfnujavaapi.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created on 2025/12/30 15:59
 *
 * @author PlagueWZK
 */
@Getter
@RequiredArgsConstructor
public enum QFNUAPI {
    //基础路径
    HOST("http://zhjw.qfnu.edu.cn"),
    BASE_URL(HOST.value + "/jsxsd"),

    //登录相关
    INDEX(BASE_URL.value + "/"),
    CAPTCHA(BASE_URL.value + "/verifycode.servlet"),
    LOGIN_POST(BASE_URL.value + "/xk/LoginToXkLdap"),

    //页面相关
    MAIN_PAGE(BASE_URL.value + "/framework/xsMain.jsp"),
    MAIN_NEW_PAGE(BASE_URL.value + "/framework/xsMain_new.jsp?t1=1"),
    MAIN_INDEX_NOTIFICATION(BASE_URL.value + "/framework/main_index_loadtzgg.jsp");

    private final String value;

}
