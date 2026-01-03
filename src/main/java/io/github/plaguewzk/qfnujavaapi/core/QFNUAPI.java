package io.github.plaguewzk.qfnujavaapi.core;

import lombok.RequiredArgsConstructor;

/**
 * Created on 2025/12/30 15:59
 *
 * @author PlagueWZK
 */
@RequiredArgsConstructor
public enum QFNUAPI {
    //基础路径
    HOST("http://zhjw.qfnu.edu.cn"),
    BASE_URL(HOST.value + "/jsxsd"),

    //登录相关
    INDEX(BASE_URL.value + "/"),
    CAPTCHA(BASE_URL.value + "/verifycode.servlet"),
    LOGIN_POST(BASE_URL.value + "/xk/LoginToXkLdap"),

    //退出登录
    LOGOUT_APP(BASE_URL.value + "/xk/LoginToXk"),
    LOGOUT_CAS("https://ids.qfnu.edu.cn/authserver/logout"),

    //页面相关
    MAIN_PAGE(BASE_URL.value + "/framework/xsMain.jsp"),
    MAIN_NEW_PAGE(BASE_URL.value + "/framework/xsMain_new.jsp"),
    MAIN_INDEX_LOAD_COURSE(BASE_URL.value + "/framework/main_index_loadkb.jsp"),
    MAIN_INDEX_NOTIFICATION_LIST(BASE_URL.value + "/framework/main_index_loadtzgg.jsp"),

    //通知相关
    MAIN_INDEX_NOTIFICATION(BASE_URL.value + "/framework/main_index_tzgg.jsp");

    public final String value;
}
