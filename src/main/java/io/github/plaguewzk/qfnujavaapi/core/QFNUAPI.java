package io.github.plaguewzk.qfnujavaapi.core;

import lombok.RequiredArgsConstructor;

/**
 * Created on 2025/12/30 15:59
 *
 * @author PlagueWZK
 */
@SuppressWarnings({"SpellCheckingInspection", "HttpUrlsUsage"})
@RequiredArgsConstructor
public enum QFNUAPI {
    //基础路径
    HOST("http://zhjw.qfnu.edu.cn"),
    BASE_URL(HOST.value + "/jsxsd"),
    INDEX(BASE_URL.value + "/"),

    //登录相关
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
    MAIN_INDEX_NOTIFICATION(BASE_URL.value + "/framework/main_index_tzgg.jsp"),

    //学期理论课表
    STUDENT_COURSE_LIST(BASE_URL.value + "/xskb/xskb_list.do"),

    //成绩查询
    GRADE_INQUIRY(BASE_URL.value + "/kscj/cjcx_list"),

    //考试信息查询
    EXAM_INFORMATION_LIST(BASE_URL.value + "/xsks/xsksap_list"),

    //学生评价
    STUDENT_FEEDBACK(BASE_URL.value + "/xspj/xspj_find.do"),
    STUDENT_EVALUATION_COURSES(BASE_URL.value + "/xspj/xspj_list.do"),
    STUDENT_EVALUATION_FORM(BASE_URL.value + "/xspj/xspj_edit.do"),
    STUDENT_EVALUATION_SAVE(BASE_URL.value + "/xspj/xspj_save.do"),
    STUDENT_EVALUATION_FINAL_SUBMIT(BASE_URL.value + "/xspj/toSavepj03wjpj.do"),

    //末尾值,方便编码不用来回改分号,不用且禁用
    UNDEFINED(null);

    public final String value;
}
