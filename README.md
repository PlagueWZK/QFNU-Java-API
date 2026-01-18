# QFNU Java API (Unofficial)

> 🚧 **当前状态：开发中 (Work in Progress)**
>
> 这是一个非官方的曲阜师范大学教务系统（强智科技系统）Java SDK 封装。

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![OkHttp](https://img.shields.io/badge/OkHttp-4.x-green)](https://square.github.io/okhttp/)
[![Jsoup](https://img.shields.io/badge/Jsoup-1.15%2B-blue)](https://jsoup.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)

## 📖 简介 | Introduction

**QFNU Java API** 是一个基于 Java 的 HTTP 客户端封装库，旨在简化与曲阜师范大学教务系统的交互流程。


## ✨ 特性 | Features

* **优雅的流式调用**：使用 Builder 模式构建客户端，API 设计简洁直观。
* **全自动会话管理**：
    * 内置 `CookieJar` 管理 Cookie。
    * **智能 Session 拦截器**：自动检测 Session 过期，并在后台静默完成“重新获取验证码 -> 登录 -> 重发请求”的流程，对上层业务无感。
* **验证码支持**：提供 `CaptchaService` 接口，内置 Tesseract OCR 实现，并支持自定义 OCR。
* **通知公告能力**：支持通知列表与详情解析（含文本与清理后的 HTML）。
* **周课表解析**：支持从主页加载当周课表并结构化解析（含课程格子详情）。
* **模块化解析**：基于 `Jsoup` 的独立解析层，将 HTML 转换为 Java Record 实体对象。
* **健壮的异常处理**：统一的异常体系，区分网络错误、解析错误和业务逻辑错误。

## 🛠️ 技术栈 | Tech Stack

* **核心语言**: Java 17
* **网络请求**: OkHttp3
* **页面解析**: Jsoup
* **验证码识别**: Tess4J (Tesseract)
* **工具库**: Lombok, SLF4J
* **构建工具**: Maven

## 🚀 快速开始 | Quick Start

### 获取学生信息

```java
import io.github.plaguewzk.qfnujavaapi.QFNUClient;
import io.github.plaguewzk.qfnujavaapi.model.entity.StudentInfo;

public class Main {
    public static void main(String[] args) {
        // 构建客户端，只需提供学号和密码
        QFNUClient client = new QFNUClient.Builder()
                .account("你的学号", "你的密码")
                .build();
        
        // 此时并未立即登录，将在发起第一个请求时自动登录
        try {
            // 获取学生服务模块
            StudentInfo info = client.studentService().getStudentInfo();

            System.out.println("姓名: " + info.name());
            System.out.println("学院: " + info.academy());
            System.out.println("专业: " + info.major());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 获取通知公告

```java
import io.github.plaguewzk.qfnujavaapi.model.entity.Notification;
import java.util.List;

List<Notification> list = client.notificationService().getList();
for (Notification item : list) {
    System.out.println(item.title() + " - " + item.publisher());
}
```

### 获取本周课表（主页当周课表）

```java
import io.github.plaguewzk.qfnujavaapi.model.entity.WeeklySchedule;

WeeklySchedule schedule = client.courseService().getCurrentWeeklyScheduleFromMainPage();
System.out.println("当前周次: " + schedule.currentWeek());
System.out.println("课程数: " + schedule.courseList().size());
```

## 📂 项目结构 | Project Structure

```Plaintext
io.github.plaguewzk.qfnujavaapi
├── QFNUClient.java     // 客户端入口
├── core                // 核心组件
│   ├── QFNUAPI.java         // 接口常量
│   ├── QFNUExecutor.java    // HTTP执行器
│   ├── SessionInterceptor.java // 会话拦截器
│   └── QFNUCookieJar.java   // Cookie管理
├── model              // 数据模型
│   └── entity              // 实体类 (StudentInfo, Notification, WeeklySchedule, CourseInfo, Course, CourseTable, Term)
├── parser             // 解析器层
│   ├── HtmlParser.java     // 解析接口
│   └── impl                // 具体实现 (StudentInfoParser, WeeklyScheduleParser, CourseInfoParser, SjmsParser, NotificationListParser, NotificationDetailParser, CourseParser(待完成), CourseTableParse(待完成))
├── service            // 业务服务层
│   ├── LoginService.java   // 登录逻辑
│   ├── StudentService.java // 学生相关业务
│   ├── CourseService.java  // 课表相关业务
│   ├── NotificationService.java // 通知相关业务
│   ├── CaptchaService.java // 验证码识别接口
│   └── impl                // 默认验证码实现 (DefaultCaptchaService)
├── util               // 工具类
│   └── Util.java
└── exception          // 自定义异常
```

## 📝 待办事项 | To-Do List

- [x] 基础网络请求架构 (OkHttp + Interceptor)

- [x] 自动登录与 Session 续期

- [x] 基础信息解析 (StudentInfo)

- [x] 验证码识别服务对接 (OCR)

- [x] 主页当周课表查询与解析 (Weekly Schedule)

- [x] 通知公告列表与详情解析

- [ ] 学期理论课表查询与解析 (CourseTable/CourseParser)

- [ ] 成绩查询与解析 (Grade & GPA)

- [ ] 考试安排查询

- [ ] 选课功能支持


## ⚠️ 免责声明 | Disclaimer

本项目仅供软件工程专业学生学习交流使用，**严禁用于任何形式的恶意爬虫、攻击教务系统或商业用途**。

- 请勿高频请求服务器，以免给教务系统带来压力。

- 开发者不对使用本工具产生的任何后果负责。

## 👤 作者 | Author
**PlagueWZK**

- GitHub: [@plaguewzk](https://github.com/PlagueWZK)
