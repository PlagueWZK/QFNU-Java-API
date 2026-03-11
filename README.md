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

## 📄 开源协议 | License

本项目基于 **Apache License 2.0** 协议开源发布。你可以在遵守协议条款的前提下自由使用、修改与分发本项目，详见根目录下的 `LICENSE` 文件。


## ✨ 特性 | Features

* **优雅的流式调用**：使用 Builder 模式构建客户端，API 设计简洁直观。
* **通用服务工厂**：支持通过 `client.service(XxxService.class)` 按需获取服务，新增服务无需修改客户端主入口。
* **全自动会话管理**：
    * 内置 `CookieJar` 管理 Cookie。
    * **智能 Session 拦截器**：自动检测 Session 过期，并在后台静默完成“重新获取验证码 -> 登录 -> 重发请求”的流程，对上层业务无感。
* **验证码支持**：提供 `CaptchaService` 接口，内置基于真实样本评估后收敛的默认 OCR 实现，并支持自定义 OCR。
* **通知公告能力**：支持通知列表与详情解析（含文本与清理后的 HTML）。
* **周课表解析**：支持从主页加载当周课表并结构化解析（含课程格子详情）。
* **模块化解析**：基于 `Jsoup` 的独立解析层，将 HTML 转换为 Java Record 实体对象。
* **健壮的异常处理**：统一的异常体系，区分网络错误、解析错误和业务逻辑错误。

## 异常体系

* `QFNUAPIException`：SDK 运行时异常总基类。
* `InvalidParameterException`：调用方传入的参数不合法。
* `AuthenticationException`：认证与会话相关异常基类。
* `InvalidCredentialsException`：账号或密码错误。
* `LoginFailedException`：登录流程重试耗尽、线程中断或依赖失败。
* `SessionRefreshException`：会话过期后自动续期失败。
* `NetworkException`：网络请求失败、响应异常或响应体读取失败。
* `ParseException`：页面解析相关异常基类。
* `PageStructureException`：教务系统页面结构变化，现有解析器无法继续工作。
* `ParsingErrorException`：页面结构仍可访问，但具体字段格式不符合预期。
* `CaptchaException`：验证码能力异常基类。
* `CaptchaInitializationException`：OCR 引擎初始化失败。
* `CaptchaRecognitionException`：OCR 识别过程失败。
* `ServiceCreationException`：服务工厂创建服务实例失败。

## 🛠️ 技术栈 | Tech Stack

* **核心语言**: Java 17
* **网络请求**: OkHttp3
* **页面解析**: Jsoup
* **验证码识别**: Tess4J (Tesseract)
* **工具库**: Lombok, SLF4J
* **构建工具**: Maven

## 🚀 快速开始 | Quick Start

### 作为 Maven 依赖引入

如果本项目已经发布到 Maven 仓库，下游项目可直接在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.github.plaguewzk</groupId>
    <artifactId>qfnu-java-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

当前项目默认产物是普通 Maven 库 JAR，而不是胖 JAR。对 SDK 来说这是正常的：下游项目通过 Maven 引入时，Maven 会根据本项目的 `pom.xml` 自动解析并拉取传递依赖，例如 `okhttp`、`jsoup`、`tess4j`、`jna`、`lept4j` 等。

### 未发布到远程仓库时的本地安装

如果项目还没有发布到 Maven Central、GitHub Packages 或私有仓库，下游开发者仍然可以在本地使用，但推荐走 Maven 本地仓库安装流程，而不是只把一个 JAR 文件随手放进 IDE 的外部库。

#### 方式一：推荐，直接从源码安装到本地 Maven 仓库

```powershell
git clone https://github.com/PlagueWZK/QFNU-Java-API.git
cd QFNU-Java-API
mvn -DskipTests install
```

执行后，Maven 会把当前 SDK 的 `jar` 和 `pom` 一并安装到开发者本机的本地仓库中。

```xml
<dependency>
    <groupId>io.github.plaguewzk</groupId>
    <artifactId>qfnu-java-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

这种方式下，Maven 能识别本 SDK 的传递依赖。

#### 方式二：只有构建产物时，手动安装 `jar + pom`

如果没有完整源码，但提供了构建出的 JAR 和对应的 `pom.xml`，也可以手动安装：

```powershell
mvn install:install-file -Dfile=qfnu-java-api-0.0.1-SNAPSHOT.jar -DpomFile=pom.xml
```

安装完成后，下游项目同样可以通过 Maven 坐标引用该 SDK，Maven 也能根据 `pom.xml` 继续解析它的传递依赖。

#### 重要说明

- 仅把 `qfnu-java-api-0.0.1-SNAPSHOT.jar` 手动复制到本地 Maven 仓库目录，通常不够。
- 仅把该 JAR 添加到 IDE 的“外部依赖库”，通常也不够。
- 原因是 Maven 解析传递依赖依赖于该构件对应的 `pom` 元数据；只有 JAR，没有 `pom`，Maven 不知道它还依赖哪些上游库。
- 如果只有 JAR 而没有对应 `pom`，那就需要下游项目自行手动补齐所有运行时依赖。

### 获取学生信息

```java
import io.github.plaguewzk.qfnujavaapi.QFNUClient;
import io.github.plaguewzk.qfnujavaapi.model.student.StudentInfo;

public class Main {
    public static void main(String[] args) {
        // 构建客户端，只需提供学号和密码
        QFNUClient client = new QFNUClient.Builder()
                .account("你的学号", "你的密码")
                .build();
        
        // 此时并未立即登录，将在发起第一个请求时自动登录
        try {
            StudentInfo info = client.service(io.github.plaguewzk.qfnujavaapi.service.StudentService.class)
                    .getStudentInfo();

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
import io.github.plaguewzk.qfnujavaapi.model.notification.Notification;
import java.util.List;

List<Notification> list = client.service(io.github.plaguewzk.qfnujavaapi.service.NotificationService.class).getList();
for (Notification item : list) {
    System.out.println(item.title() + " - " + item.publisher());
}
```

### 获取本周课表（主页当周课表）

```java
import io.github.plaguewzk.qfnujavaapi.model.course.WeeklySchedule;

WeeklySchedule schedule = client.service(io.github.plaguewzk.qfnujavaapi.service.CourseService.class)
        .getCurrentWeeklyScheduleFromMainPage();
System.out.println("当前周次: " + schedule.currentWeek());
System.out.println("课程数: " + schedule.courseList().size());
```

### 获取学期理论课表

```java
import io.github.plaguewzk.qfnujavaapi.model.course.Course;
import io.github.plaguewzk.qfnujavaapi.model.course.CourseTable;
import io.github.plaguewzk.qfnujavaapi.model.course.Term;

CourseTable table = client.service(io.github.plaguewzk.qfnujavaapi.service.CourseService.class)
        .getCourseTable(Term.current(), 1);

for (Course course : table.courses()) {
    System.out.printf("%s %s %s %s%n",
            course.weekday(),
            course.courseName(),
            course.section(),
            course.location());
}
```

`Course` 现在包含 `weekday` 字段，表示课程位于周一到周日中的哪一天。

## 📂 项目结构 | Project Structure

```Plaintext
io.github.plaguewzk.qfnujavaapi
├── QFNUClient.java     // 客户端入口
├── core                // 核心组件
│   ├── QFNUAPI.java         // 接口常量
│   ├── QFNUContext.java     // 共享运行时上下文
│   ├── QFNUExecutor.java    // HTTP执行器
│   ├── ServiceFactory.java  // 通用服务工厂
│   ├── SessionInterceptor.java // 会话拦截器
│   └── QFNUCookieJar.java   // Cookie管理
├── model              // 数据模型
│   ├── course              // 课程域模型 (Course, Weekday, Weeks, Section, SectionConstant, CourseInfo, CourseTable, Term, WeeklySchedule)
│   ├── notification        // 通知域模型 (Notification, NotificationDetail)
│   └── student             // 学生域模型 (StudentInfo)
├── parser             // 解析器层
│   ├── HtmlParser.java     // 解析接口
│   └── impl                // 具体实现 (StudentInfoParser, WeeklyScheduleParser, CourseInfoParser, SjmsParser, NotificationListParser, NotificationDetailParser, CourseParser, CourseTableParse)
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

- [x] 学期理论课表查询与解析 (CourseTable/CourseParser)

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
