# QFNU-Java-API 开发指南

> 曲阜师范大学教务系统（强智科技）的非官方 Java SDK — 完整的使用指南与贡献文档。

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![OkHttp](https://img.shields.io/badge/OkHttp-4.x-green)](https://square.github.io/okhttp/)
[![Jsoup](https://img.shields.io/badge/Jsoup-1.15%2B-blue)](https://jsoup.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)

---

## 目录

- [第一部分：使用者指南](#第一部分使用者指南)
  - [1.1 简介与特性](#11-简介与特性)
  - [1.2 环境要求与依赖引入](#12-环境要求与依赖引入)
  - [1.3 配置说明](#13-配置说明)
  - [1.4 快速开始](#14-快速开始)
  - [1.5 API 参考](#15-api-参考)
  - [1.6 验证码自定义](#16-验证码自定义)
  - [1.7 扩展模块开发](#17-扩展模块开发)
  - [1.8 常见问题](#18-常见问题)
- [第二部分：贡献者指南](#第二部分贡献者指南)
  - [2.1 项目架构深度解析](#21-项目架构深度解析)
  - [2.2 核心机制详解](#22-核心机制详解)
  - [2.3 异常体系](#23-异常体系)
  - [2.4 开发环境搭建](#24-开发环境搭建)
  - [2.5 添加新功能的标准流程](#25-添加新功能的标准流程)
  - [2.6 编码约定](#26-编码约定)
  - [2.7 测试指南](#27-测试指南)
  - [2.8 提交规范](#28-提交规范)
  - [2.9 发布流程](#29-发布流程)
- [附录](#附录)

---

## 第一部分：使用者指南

### 1.1 简介与特性

QFNU-Java-API 是曲阜师范大学教务管理系统的非官方 Java SDK，底层为强智科技教务平台。通过 HTTP + HTML 解析方式封装了登录、课表、成绩、考试、通知、评教等功能，提供类型安全的 Java API。

**核心特性：**

- **Builder 模式构建客户端** — 通过 `QFNUClient.Builder` 配置账号、密码、验证码服务和扩展模块，`client.service(XxxService.class)` 按需获取服务
- **自动登录与 Session 续期** — Session 过期时后台静默重新登录，上层业务无感知
- **内置验证码 OCR** — 基于 Tesseract（Tess4J），支持多策略预处理流水线（缩放 → 二值化 → 降噪 → 识别），可通过 `CaptchaService` 接口自定义
- **课表查询** — 当周周课表 + 学期理论课表，支持按周筛选
- **成绩查询** — 支持按学期、课程性质、课程名等多条件组合筛选
- **考试安排查询** — 按学期查询考试时间、地点、座位号
- **通知公告** — 列表浏览 + 详情查看
- **学生评教** — 双策略自动评分（最接近满分 / 最接近 90 分）、一键评完所有课程、最终提交
- **独立解析层** — 基于 Jsoup 的 HTML → Java Record 解析，与业务逻辑分离
- **可扩展** — 通过 `QFNUModule` 接口注册自定义 Parser 和 Service

**技术栈：**

| 组件 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 运行环境 |
| OkHttp3 | 4.12.0 | HTTP 客户端 |
| Jsoup | 1.17.2 | HTML 解析 |
| Tess4J (Tesseract) | 5.10.0 | 验证码 OCR 识别 |
| Lombok | 1.18.42 | 样板代码消除 |
| SLF4J + Logback | 2.0.12 / 1.5.22 | 日志 |
| JUnit Jupiter | 5.10.2 | 单元测试 |

### 1.2 环境要求与依赖引入

**运行环境：**

- JDK 17 或更高版本
- Maven 3.6+（用于构建和测试）

**方式一：克隆仓库本地安装**

```bash
git clone https://github.com/PlagueWZK/QFNU-Java-API.git
cd QFNU-Java-API
mvn -DskipTests install
```

执行后 Maven 会将 jar 和 pom 安装到本地仓库（`~/.m2/repository`）。

**方式二：下游项目引入 Maven 依赖**

```xml
<dependency>
    <groupId>io.github.plaguewzk</groupId>
    <artifactId>qfnu-java-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

> **注意**：项目目前为 SNAPSHOT 版本，尚未发布到 Maven Central。如需使用，请先按方式一安装到本地仓库。

### 1.3 配置说明

QFNU-Java-API 支持两种凭据配置方式。

**方式一：Builder 传参（推荐）**

在代码中直接传入学号和密码：

```java
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .build();
```

这是推荐方式，无需额外配置文件。

**方式二：properties 配置文件**

将项目根目录下的 `config.qfnuapi.properties.example` 复制为 `config.qfnuapi.properties`，填入真实凭据：

```properties
qfnu.account=202xxxxxxx
qfnu.password=your-password
```

> `config.qfnuapi.properties` 已被 `.gitignore` 忽略，不会被提交到版本控制。

**自定义超时设置：**

```java
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .timeout(Duration.ofSeconds(15), Duration.ofSeconds(30))  // 连接超时 / 读取超时
        .build();
```

默认连接超时 10 秒，读取超时 10 秒。

### 1.4 快速开始

以下示例从最简单到最复杂，逐步展示各功能用法。

**1. 获取学生信息**

```java
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .build();

StudentInfo info = client.service(StudentService.class).getStudentInfo();
System.out.println("姓名: " + info.name());
System.out.println("学号: " + info.studentId());
System.out.println("学院: " + info.academy());
System.out.println("专业: " + info.major());
System.out.println("班级: " + info.className());
```

`StudentInfo` 是一个 Java Record，包含 `name()`、`studentId()`、`academy()`、`major()`、`className()` 五个字段。

**2. 获取通知公告**

```java
List<Notification> list = client.service(NotificationService.class).getList();
for (Notification item : list) {
    System.out.println(item.title() + " — " + item.publisher());
}
```

`getList()` 返回的通知已自动填充详细信息（发布者、时间、正文）。

**3. 获取本周课表**

```java
WeeklySchedule schedule = client.service(CourseService.class)
        .getCurrentWeeklyScheduleFromMainPage();
System.out.println("第 " + schedule.currentWeek() + " 周，共 " + schedule.courseList().size() + " 门课");

schedule.courseList().forEach(c -> {
    System.out.printf("%s 周%s %s %s\n",
        c.weeks(), c.weekday(), c.sectionInfo(), c.courseName());
});
```

**4. 获取学期理论课表**

```java
// 获取当前学期第 1 周的课表
CourseTable table = client.service(CourseService.class)
        .getCourseTable(Term.current(), 1);

table.courses().forEach(c ->
    System.out.printf("%s %s %s @ %s\n",
        c.weekday(), c.courseName(), c.section(), c.location()));

// 获取当前学期默认课表（不指定周数）
CourseTable currentTable = client.service(CourseService.class)
        .getCurrentCourseTable();
```

**5. 成绩查询**

```java
// 默认查询全部成绩
List<CourseGrade> grades = client.service(GradeService.class).getGradeList();
grades.forEach(g -> System.out.printf("%s: %s (%s学分)\n",
        g.courseName(), g.grade(), g.credit()));

// 按条件查询
GradeQuery query = GradeQuery.builder()
        .kksj("2025-2026-1")   // 开课学期
        .kcmc("Java")           // 课程名称（模糊匹配）
        .build();

GradeReport report = client.service(GradeService.class).getGradeReport(query);
System.out.println("平均绩点: " + report.gpa());
System.out.println("平均分: " + report.averageScore());
report.grades().forEach(g -> System.out.println(g.courseName() + ": " + g.grade()));
```

`GradeQuery` 使用 Builder 模式，支持 `kksj`（开课时间）、`kcxz`（课程性质）、`kcmc`（课程名称）三个可选条件。不设置任何条件则查询全部成绩。

`GradeReport` 包含 `queryCondition()`（查询条件描述）、`totalCredits()`、`gpa()`、`averageScore()`、`weightedAverage()` 和 `grades()` 列表。

**6. 考试安排查询**

```java
// 默认查询当前学期
List<ExamSchedule> exams = client.service(ExamScheduleService.class)
        .getExamSchedules();
exams.forEach(e -> System.out.printf("%s | %s | %s | %s | 座位%s\n",
        e.courseName(), e.examTime(), e.examRoom(), e.campus(), e.seatNumber()));

// 按学期查询
ExamScheduleQuery query = ExamScheduleQuery.builder()
        .xnxqid(Term.current())
        .build();
List<ExamSchedule> filteredExams = client.service(ExamScheduleService.class)
        .getExamSchedules(query);
```

`ExamSchedule` 包含 12 个字段，包括 `courseName()`、`examTime()`、`examRoom()`、`seatNumber()`、`admissionNo()` 等。

**7. 学生评教**

```java
StudentService svc = client.service(StudentService.class);

// 1. 获取待评价入口列表
List<EvaluationEntry> entries = svc.getEvaluationList();

// 2. 一键评完所有课程（前 40% 高分，剩余接近 90 分）
List<EvaluationResult> results = svc.autoEvaluateAll(entries.get(0));
results.forEach(r -> {
    if (r.success()) {
        System.out.printf("%s → %.2f 分 [%s]\n",
            r.course().courseName(), r.score(), r.scheme());
    } else {
        System.err.printf("%s → 失败: %s\n",
            r.course().courseName(), r.errorMessage());
    }
});

// 3. 检查无误后最终提交（提交后不可修改）
svc.finalSubmit(entries.get(0));
```

**评教策略说明：**

| 策略 | 枚举值 | 规则 |
|------|--------|------|
| 最接近满分 | `CLOSEST_TO_FULL` | 所有指标选择最高分选项，总分约 98 分 |
| 最接近 90 分 | `CLOSEST_TO_90` | 动态搭配高低分，总分尽量接近 90 分 |

`autoEvaluateAll()` 策略：前 40%（向下取整）课程使用 `CLOSEST_TO_FULL`，剩余使用 `CLOSEST_TO_90`。

### 1.5 API 参考

#### StudentService — 学生与评教

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getStudentInfo()` | `StudentInfo` | 获取当前登录学生的基本信息 |
| `getEvaluationList()` | `List<EvaluationEntry>` | 获取待评价入口列表 |
| `getEvaluationCourses(EvaluationEntry)` | `List<EvaluationCourse>` | 获取指定入口下的待评课程 |
| `getEvaluationForm(EvaluationCourse)` | `EvaluationFormData` | 获取指定课程的评教表单（含指标和选项） |
| `autoEvaluate(EvaluationCourse, EvaluationScheme)` | `EvaluationResult` | 为单个课程自动评分并保存 |
| `autoEvaluateAll(EvaluationEntry)` | `List<EvaluationResult>` | 一键评完所有课程（前 40% 高分，后 60% 接近 90 分） |
| `submitEvaluation(EvaluationSubmission)` | `void` | 提交评教表单（一般由 `autoEvaluate` 内部调用） |
| `finalSubmit(EvaluationEntry)` | `void` | 最终提交评教（**提交后不可修改**） |

#### CourseService — 课表

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getCurrentWeeklyScheduleFromMainPage()` | `WeeklySchedule` | 获取当天所在周的周课表（已弃用，推荐使用 `getCourseTable`） |
| `getCourseTable(Term, int week)` | `CourseTable` | 获取指定学期、指定周的学期理论课表 |
| `getCurrentCourseTable()` | `CourseTable` | 获取当前学期默认课表 |

#### GradeService — 成绩

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getGradeList()` | `List<CourseGrade>` | 获取全部课程成绩（默认查询条件） |
| `getGradeList(GradeQuery)` | `List<CourseGrade>` | 按条件查询课程成绩 |
| `getGradeReport()` | `GradeReport` | 获取完整成绩报告（含 GPA、平均分等统计） |
| `getGradeReport(GradeQuery)` | `GradeReport` | 按条件获取完整成绩报告 |

#### ExamScheduleService — 考试安排

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getExamSchedules()` | `List<ExamSchedule>` | 获取当前学期所有考试安排 |
| `getExamSchedules(ExamScheduleQuery)` | `List<ExamSchedule>` | 按学期查询考试安排 |

#### NotificationService — 通知公告

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getList()` | `List<Notification>` | 获取通知列表（已自动填充详情） |
| `fillDetail(Notification)` | `Notification` | 为指定通知填充详情（发布者、时间、正文） |

#### LoginService — 登录与会话

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `autoLogin(int repeatCount)` | `void` | 执行自动登录，最多重试 `repeatCount` 次 |
| `logout()` | `boolean` | 注销登录并清理本地 Cookie |

> **注意**：`LoginService` 通常不需要手动调用。登录会在 `QFNUClient` 构建时自动执行，Session 过期时也会自动续期。

### 1.6 验证码自定义

SDK 内置基于 Tesseract OCR 的默认验证码识别服务（`DefaultCaptchaService`），无需额外配置即可使用。如果默认识别率不满足需求，可以实现 `CaptchaService` 接口替换。

**默认识别流程：**

```
原始验证码图片
  → 3x 双三次插值放大（Bicubic scaling）
  → 二值化（固定阈值 170）
  → 噪声去除（8 邻域孤立点过滤）
  → Tesseract OCR（PSM 8 模式，仅识别数字和小写字母）
  → 正则验证（^[0-9a-z]{4}$）
```

**自定义实现：**

```java
public class MyCaptchaService implements CaptchaService {
    @Override
    public String recognize(byte[] imageBytes) {
        // 自定义识别逻辑
        // 返回 4 位验证码字符串（数字 + 小写字母）
        return "abcd";
    }
}

// 注入自定义服务
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .captchaService(new MyCaptchaService())
        .build();
```

**关于 `CaptchaStrategyEvaluatorApp`：**

项目提供了验证码策略评估工具，可通过 Maven 运行：

```bash
mvn exec:java -Dexec.mainClass="io.github.plaguewzk.qfnujavaapi.service.impl.CaptchaStrategyEvaluatorApp"
```

该工具对联机验证码批量评估不同预处理策略的成功率，方便调优。

### 1.7 扩展模块开发

通过 `QFNUModule` 接口，下游项目可以注册自定义的 Parser 和 Service，无需修改 SDK 源码。

**QFNUModule 接口：**

```java
@FunctionalInterface
public interface QFNUModule {
    void configure(ParserRegistry parsers, ServiceRegistry services);
}
```

**注册自定义 Parser：**

```java
public class CustomModule implements QFNUModule {
    @Override
    public void configure(ParserRegistry parsers, ServiceRegistry services) {
        // 注册自定义解析器（无依赖）
        parsers.registerParser(CustomParser.class,
                resolver -> new CustomParser());

        // 注册依赖其他 Parser 的解析器
        parsers.registerParser(CompositeParser.class,
                resolver -> new CompositeParser(
                        resolver.parser(CustomParser.class)
                ));
    }
}
```

**注册自定义 Service：**

```java
public class CustomModule implements QFNUModule {
    @Override
    public void configure(ParserRegistry parsers, ServiceRegistry services) {
        // 注册自定义服务（依赖上下文 + Parser）
        services.registerService(CustomService.class,
                resolver -> new CustomService(
                        resolver.context(),
                        resolver.parser(CustomParser.class)
                ));
    }
}
```

**安装模块：**

```java
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .install(new CustomModule())
        .build();

// 即可以类型安全的方式获取自定义服务
CustomService svc = client.service(CustomService.class);
```

**依赖注入规则：**

- `resolver.context()` — 获取 `QFNUContext`（含 executor、账号密码、CaptchaService）
- `resolver.executor()` — 直接获取 `QFNUExecutor`
- `resolver.parser(Class<T>)` — 获取已注册的 Parser 实例（自动懒加载 + 缓存）
- `resolver.service(Class<T>)` — 获取已注册的 Service 实例

> **设计约束**：Parser 可以依赖其他 Parser，Service 可以依赖 Parser，但 **Parser 不应依赖 Service**。依赖通过构造函数声明，由 `DefaultComponentResolver` 在实例化时自动注入。内置循环依赖检测，若检测到循环链路会抛出 `ServiceCreationException` 并打印完整链路。

### 1.8 常见问题

**Q: Session 过期后怎么处理？**

SDK 内置 Session 自动续期机制。当 `SessionInterceptor` 检测到响应被重定向到登录页面或响应体包含登录表单时，会自动执行登录并重试原请求。整个过程对上层业务透明，无需手动处理。

**Q: 验证码识别失败怎么办？**

`LoginService.autoLogin()` 最多重试 20 次。每次重试会重新获取验证码图片并调用 OCR 识别。如果 20 次均失败，会抛出 `LoginFailedException`。可以通过以下方式改善：

1. 实现自定义 `CaptchaService` 提高识别率
2. 连接远程打码平台（如超级鹰）
3. 使用 `CaptchaStrategyEvaluatorApp` 评估不同预处理策略

**Q: 如何调整日志级别？**

项目使用 SLF4J + Logback。修改 `src/main/resources/logback.xml`：

```xml
<!-- SDK 包日志级别设为 DEBUG 可查看详细网络请求和解析过程 -->
<logger name="io.github.plaguewzk.qfnujavaapi" level="DEBUG"/>

<!-- 生产环境建议 INFO 或 WARN -->
<logger name="io.github.plaguewzk.qfnujavaapi" level="INFO"/>
```

> **注意**：密码和验证码结果的日志只会出现在 DEBUG 级别，生产环境使用 INFO 级别不会泄露敏感信息。

**Q: 并发请求是否安全？**

- `QFNUClient` 是线程安全的，可以多线程共享
- `QFNUCookieJar` 使用 `ConcurrentHashMap` 存储 Cookie，支持线程安全读写
- `DefaultComponentResolver` 使用 `ConcurrentHashMap` 缓存组件实例
- Session 续期使用 `synchronized` 块，确保同一时刻只有一个线程执行登录
- 各 Service 实例是无状态的，可以安全共享

**Q: 请求超时了怎么办？**

可以通过 `Builder.timeout()` 设置更长的超时时间：

```java
QFNUClient client = new QFNUClient.Builder()
        .account("202xxxxxxx", "your-password")
        .timeout(Duration.ofSeconds(30), Duration.ofSeconds(60))
        .build();
```

如果教务系统本身不可达，会抛出 `NetworkException`。

---

## 第二部分：贡献者指南

### 2.1 项目架构深度解析

#### 分层架构

```
┌──────────────────────────────────────────────────────────┐
│  QFNUClient (入口层)                                      │
│  Builder 模式构建，提供 service(Class<T>) 统一服务获取      │
├──────────────────────────────────────────────────────────┤
│  core/ (核心框架层)                                        │
│  QFNUExecutor → SessionInterceptor → QFNUCookieJar       │
│  ComponentRegistry + DefaultComponentResolver (DI容器)    │
│  QFNUContext (运行时上下文)                                │
├──────────────────────┬───────────────────────────────────┤
│  service/ (业务服务层) │  parser/ (解析层)                  │
│  LoginService        │  HtmlParser<T> 接口                 │
│  StudentService      │  impl/ (13 个解析器)                │
│  CourseService       │  ParserUtils (公共工具)             │
│  GradeService        │                                     │
│  ExamScheduleService │                                     │
│  NotificationService │                                     │
├──────────────────────┴───────────────────────────────────┤
│  model/ (领域模型层)                                       │
│  course/ | evaluation/ | exam/ | grade/ | notification/  │
│  student/ (全部为 Java Record，不可变)                     │
└──────────────────────────────────────────────────────────┘
```

#### 各层职责

| 层 | 包路径 | 职责 |
|----|--------|------|
| 入口层 | `QFNUClient.java` | Builder 模式构建客户端，组装各层组件，暴露统一 API |
| 核心框架层 | `core/` | HTTP 执行、Cookie 管理、Session 拦截、组件注册与 DI |
| 业务服务层 | `service/` | 组合 HTTP 调用 + Parser，暴露面向业务的 API |
| 解析层 | `parser/` | `HtmlParser<T>` 接口实现，将 HTML 字符串解析为 Java Record |
| 领域模型层 | `model/` | 不可变 Java Record，按业务领域分 5 个子包 |

#### 包依赖规则

```
入口层 (QFNUClient)
  ↓
业务服务层 (service/) ──→ 解析层 (parser/)
  ↓                           ↓
核心框架层 (core/) ←──────────┘
  ↓
领域模型层 (model/)
```

**关键规则**：
- **Parser 可以依赖其他 Parser** — 例如 `CourseTableParse` 依赖 `CourseParser`，在注册时通过 `resolver.parser(CourseParser.class)` 声明
- **Service 可以依赖 Parser** — 通过构造函数接收 `HtmlParser<T>` 接口
- **Parser 不应依赖 Service** — 保持解析层的纯粹性和可测试性

#### 核心文件索引

| 文件 | 角色 |
|------|------|
| `QFNUClient.java` | 客户端入口，Builder 模式 |
| `core/QFNUAPI.java` | 所有 API 端点的枚举定义 |
| `core/QFNUExecutor.java` | OkHttpClient 封装，GET/POST/formPost |
| `core/QFNUCookieJar.java` | Cookie 持久化，基于 ConcurrentHashMap |
| `core/SessionInterceptor.java` | OkHttp Interceptor，检测 Session 过期并自动续期 |
| `core/QFNUContext.java` | 运行时上下文（executor + 凭据 + CaptchaService） |
| `core/ComponentRegistry.java` | 同时实现 ParserRegistry 和 ServiceRegistry |
| `core/DefaultComponentResolver.java` | DI 容器，懒加载 + 缓存 + 循环依赖检测 |
| `core/QFNUModule.java` | 扩展接口 `@FunctionalInterface` |
| `core/QFNUBuiltinModule.java` | 内置模块，注册所有默认 Parser 和 Service |
| `core/ComponentProvider.java` | 组件工厂函数 `@FunctionalInterface` |
| `core/ComponentResolver.java` | 组件解析器接口（context/executor/parser/service） |
| `core/ParserRegistry.java` | Parser 注册扩展点接口 |
| `core/ServiceRegistry.java` | Service 注册扩展点接口 |

### 2.2 核心机制详解

#### 组件注册与依赖注入

这是项目最核心的扩展机制。旧版本使用 `ParserFactory` 和 `ServiceFactory` 两个独立的工厂类，存在相互引用和时序耦合问题。最新重构后的设计：

**1. 注册阶段（QFNUClient 构建时）：**

```
QFNUBuiltinModule.configure(registry, registry)
  → registry.registerParser(CourseParser.class, resolver -> new CourseParser())
  → registry.registerService(CourseService.class, resolver -> new CourseService(...))

用户扩展模块:
  → customModule.configure(registry, registry)
```

`ComponentRegistry` 同时实现 `ParserRegistry` 和 `ServiceRegistry`，内部用两个 `LinkedHashMap<Class<?>, ComponentProvider<?>>` 存储 Parser 和 Service 的懒加载工厂函数。`LinkedHashMap` 保证注册顺序，方便调试。

**2. 解析阶段（首次调用时）：**

```java
// 用户调用
client.service(CourseService.class);

// 内部流程:
DefaultComponentResolver.service(CourseService.class)
  → serviceCache.get(CourseService.class)  // 检查缓存
  → null? → checkCircularDependency()       // 循环依赖检测
  → serviceRegistry.get(CourseService.class) // 查找 Provider
  → provider.get(resolver)                   // 调用工厂函数
  → resolver.parser(WeeklyScheduleParser.class) // 触发 Parser 懒加载
  → serviceCache.putIfAbsent()              // 缓存实例
```

**3. 循环依赖检测：**

使用 `ThreadLocal<LinkedHashSet<Class<?>>>` 追踪当前线程的创建链。当检测到同一类型再次出现时，抛出 `ServiceCreationException` 并打印完整链路，例如：

```
检测到组件循环依赖，无法创建解析器 [CourseParser]
创建链路: CourseTableParse → CourseParser → CourseTableParse (回到起点)
```

**4. 线程安全：**

- 缓存使用 `ConcurrentHashMap`，支持多线程并发访问
- `get` + `putIfAbsent` 模式：多线程可能同时穿透缓存，但 `putIfAbsent` 确保最终只缓存第一个成功的实例
- 显式避免了 `computeIfAbsent`（其内置递归检测会在 ThreadLocal 检查前触发）

#### HTTP 执行链

```
QFNUClient
  → QFNUExecutor (OkHttpClient 封装)
     → SessionInterceptor (OkHttp Interceptor)
        → 检测 Session 过期 (URL 重定向 / Body 登录表单)
        → synchronized 执行自动登录
        → 重试原请求
     → QFNUCookieJar (CookieJar 实现)
        → ConcurrentHashMap<String, List<Cookie>>
        → 过期 Cookie 自动清理
```

**QFNUExecutor** 提供的方法：

| 方法 | 用途 |
|------|------|
| `executeGet(QFNUAPI)` | 简单 GET 请求 |
| `executeGet(QFNUAPI, Map<String,String>)` | 带 Query 参数的 GET |
| `executePost(QFNUAPI, Map<String,String>, String/QFNUAPI)` | 标准 POST（FormBody） |
| `executeFormPost(QFNUAPI, String, String/QFNUAPI)` | URL 编码字符串 POST（支持重复 key） |
| `buildUrl(QFNUAPI, Map<String,String>)` | 构建带参数的完整 URL |
| `executeForBytes(Request)` | 返回响应字节数组（用于验证码图片） |
| `executeForString(Request)` | 返回响应字符串（用于页面解析） |

#### Session 过期检测与自动续期

`SessionInterceptor` 的判断逻辑（`isSessionExpired` 方法）：

1. **跳过检测的请求**：首页、登录接口、验证码接口、退出登录接口 — 这些请求不需要登录
2. **URL 重定向检测**：检查 response URL 是否被重定向到 `/jsxsd/` 或 `/xk/LoginToXk`（登录页面）
3. **Body 内容检测**：检查响应体是否包含登录表单标记（"请输入账号" + "请输入密码" + "请输入验证码"）

一旦检测到过期：

```
response.close()                    // 关闭过期响应
synchronized (this) {               // 同步块，防止并发登录
    loginAction.run()               // 调用 QFNUClient.login()
      → service(LoginService.class).autoLogin(10)
    chain.proceed(newRequest)       // 用新的 Cookie 重试原请求
}
```

登录失败抛出 `SessionRefreshException`。

#### 验证码 OCR 策略

`DefaultCaptchaService` 支持多策略流水线，通过 `StrategySpec` record 定义每个策略的参数：

| 参数 | 说明 | 默认主策略值 |
|------|------|------------|
| `name` | 策略名称 | `fixed-170-psm8` |
| `pageSegMode` | Tesseract 页面分割模式 | `8`（单个单词，PSM_SINGLE_WORD） |
| `pipeline` | 预处理流水线 | `FIXED_THRESHOLD` |
| `fixedThreshold` | 二值化阈值 | `170` |
| `scaleFactor` | 缩放倍数 | `3.0` |

**三种预处理流水线：**

| 流水线 | 枚举 | 说明 |
|--------|------|------|
| `FIXED_THRESHOLD` | 固定阈值二值化 | 灰度 < 阈值 → 黑色，默认阈值 170 |
| `OTSU` | 大津算法 | 自适应计算最优阈值 |
| `CONTRAST` | 对比度归一化 | 拉伸至全色域 [0, 255] |

**Tesseract 配置：**

```java
tesseract.setLanguage("eng");
tesseract.setPageSegMode(8);  // PSM_SINGLE_WORD
tesseract.setVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyz");
tesseract.setVariable("load_system_dawg", "F");
tesseract.setVariable("load_freq_dawg", "F");
tesseract.setVariable("user_defined_dpi", "300");
```

- 字符白名单限制为数字和小写字母（教务验证码只包含这些字符）
- 关闭词典和频率 DAWG，避免"纠正"成常见英文单词
- 设置 DPI 300 提高识别精度

识别结果通过正则 `^[0-9a-z]{4}$` 验证，不符合的自动尝试下一个策略。

### 2.3 异常体系

所有异常继承自 `QFNUAPIException (RuntimeException)`，使用者无需强制 try-catch。

#### 异常树状图

```
QFNUAPIException (RuntimeException)
├── AuthenticationException            // 认证相关异常
│   ├── InvalidCredentialsException    // 账号或密码错误（不可重试）
│   ├── LoginFailedException           // 登录失败（达到最大重试次数）
│   └── SessionRefreshException        // Session 刷新失败
├── NetworkException                   // 网络请求失败
├── ParseException                     // HTML 解析异常
│   ├── PageStructureException         // 页面结构变化
│   └── ParsingErrorException          // 解析过程错误
├── CaptchaException                   // 验证码相关异常
│   ├── CaptchaInitializationException // 验证码引擎初始化失败
│   └── CaptchaRecognitionException    // 验证码识别失败
├── InvalidParameterException          // 参数校验失败
└── ServiceCreationException           // 服务/组件创建失败（含循环依赖）
```

#### 异常触发场景

| 异常 | 触发场景 | 处理建议 |
|------|----------|----------|
| `InvalidCredentialsException` | 登录时服务端返回"密码错误"或"账号不存在" | 检查账号密码是否正确 |
| `LoginFailedException` | 20 次验证码识别 + 登录重试均失败 | 检查网络、验证码识别率、教务系统状态 |
| `SessionRefreshException` | Session 过期后自动重新登录失败 | 检查网络和教务系统状态 |
| `NetworkException` | HTTP 请求失败、响应码非 2xx、读取响应体失败 | 检查网络连通性和教务系统可达性 |
| `PageStructureException` | 页面结构变化（预期元素不存在、API 地址非法） | 教务系统可能更新了页面结构，需要更新解析器 |
| `ParsingErrorException` | 解析过程中发生格式异常 | 检查 HTML fixture 或联系维护者 |
| `CaptchaInitializationException` | Tesseract 引擎初始化失败（训练数据缺失） | 检查 tessdata 文件是否在 classpath 中 |
| `CaptchaRecognitionException` | 所有 OCR 策略均未识别出合法验证码 | 考虑自定义 CaptchaService 或调整策略参数 |
| `InvalidParameterException` | 用户传入的账号/密码/参数为 null 或非法 | 检查调用代码的参数 |
| `ServiceCreationException` | 组件循环依赖或服务类型未注册 | 检查模块注册代码的依赖关系 |

### 2.4 开发环境搭建

**前置条件：**

| 工具 | 最低版本 | 说明 |
|------|----------|------|
| JDK | 17 | 使用 Record、Switch 表达式等特性 |
| Maven | 3.6+ | 构建和依赖管理 |
| Git | 任意 | 版本控制 |

**步骤 1：克隆仓库**

```bash
git clone https://github.com/PlagueWZK/QFNU-Java-API.git
cd QFNU-Java-API
```

**步骤 2：配置本地凭据**

```bash
cp config.qfnuapi.properties.example config.qfnuapi.properties
```

编辑 `config.qfnuapi.properties`，填入真实教务系统学号和密码。此文件已被 `.gitignore` 忽略。

**步骤 3：编译项目**

```bash
mvn compile
```

**步骤 4：运行测试**

```bash
# 运行所有单元测试（不需要连接教务系统）
mvn test

# 运行集成测试（需要凭据和网络）
mvn test -Dgroups="integration"
```

**IDE 配置：**

项目使用 Lombok 注解处理器，需要在 IDE 中启用：

- **IntelliJ IDEA**：安装 Lombok 插件，在 Settings → Build → Compiler → Annotation Processors 中启用 "Enable annotation processing"
- **Eclipse**：运行 Lombok jar 安装器，或在 eclipse.ini 中添加 `-javaagent:lombok.jar`

### 2.5 添加新功能的标准流程

以"考试安排"功能为例，展示从零添加一个完整功能模块的标准 6 步流程。

#### 步骤概览

```
1. model/  → 定义领域 Record（数据模型）
2. parser/ → 实现 HtmlParser<T>（HTML 解析器）
3. service/ → 创建 Service 类（业务逻辑）
4. core/QFNUAPI.java → 添加 API 端点
5. core/QFNUBuiltinModule → 注册 Parser 和 Service
6. 测试 → 编写单元测试 + 集成测试
```

#### 步骤 1：定义领域模型

在 `model/exam/` 下创建 Record：

```java
// ExamSchedule.java
package io.github.plaguewzk.qfnujavaapi.model.exam;

public record ExamSchedule(
    String index,        // 序号
    String campus,       // 校区
    String session,      // 场次
    String courseId,     // 课程编号
    String courseName,   // 课程名称
    String instructor,   // 授课教师
    String examTime,     // 考试时间
    String examRoom,     // 考试地点
    String seatNumber,   // 座位号
    String admissionNo,  // 准考证号
    String remarks,      // 备注
    String operation     // 操作
) {}
```

```java
// ExamScheduleQuery.java
package io.github.plaguewzk.qfnujavaapi.model.exam;

public record ExamScheduleQuery(String xnxqid) {
    public static Builder builder() { return new Builder(); }

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        if (xnxqid != null) map.put("xnxqid", xnxqid);
        return map;
    }

    public static class Builder {
        private String xnxqid;
        public Builder xnxqid(Term term) { this.xnxqid = term.toString(); return this; }
        public ExamScheduleQuery build() { return new ExamScheduleQuery(xnxqid); }
    }
}
```

**设计原则：**

- 核心数据模型使用 `record`（不可变、自动生成 equals/hashCode/toString）
- 查询参数对象使用 Builder 模式（可选参数灵活组合）
- 提供 `toMap()` 方法将参数转为 HTTP 请求的 Query/Form 参数

#### 步骤 2：实现 HTML 解析器

在 `parser/impl/` 下实现 `HtmlParser<T>` 接口：

```java
// ExamScheduleParser.java
package io.github.plaguewzk.qfnujavaapi.parser.impl;

import io.github.plaguewzk.qfnujavaapi.model.exam.ExamSchedule;
import io.github.plaguewzk.qfnujavaapi.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class ExamScheduleParser implements HtmlParser<List<ExamSchedule>> {

    @Override
    public List<ExamSchedule> parser(String html) {
        Document doc = Jsoup.parse(html);
        List<ExamSchedule> schedules = new ArrayList<>();

        Element table = doc.selectFirst("table");
        if (table == null) return schedules;

        Elements rows = table.select("tr");
        for (int i = 1; i < rows.size(); i++) {  // 跳过表头
            Elements cells = rows.get(i).select("td");
            if (cells.size() < 10) continue;

            schedules.add(new ExamSchedule(
                cells.get(0).text(),
                cells.get(1).text(),
                // ... 映射剩余字段
            ));
        }
        return schedules;
    }
}
```

**解析器编写要点：**

- 使用 Jsoup 的 CSS 选择器定位元素
- 跳过表头行（从 `i=1` 开始）
- 处理异常情况：表格不存在、列数不足等，返回空列表而非抛异常
- 使用 `ParserUtils` 中的公共方法（如存在）

#### 步骤 3：创建 Service

在 `service/` 下创建业务服务：

```java
// ExamScheduleService.java
package io.github.plaguewzk.qfnujavaapi.service;

public class ExamScheduleService {
    private final QFNUExecutor qfnuExecutor;
    private final HtmlParser<List<ExamSchedule>> examScheduleParser;

    public ExamScheduleService(QFNUContext context,
                                HtmlParser<List<ExamSchedule>> examScheduleParser) {
        this.qfnuExecutor = context.executor();
        this.examScheduleParser = Objects.requireNonNull(examScheduleParser, "examScheduleParser");
    }

    public List<ExamSchedule> getExamSchedules() {
        return getExamSchedules(ExamScheduleQuery.builder()
                .xnxqid(Term.current()).build());
    }

    public List<ExamSchedule> getExamSchedules(ExamScheduleQuery query) {
        String html = qfnuExecutor.executePost(
                QFNUAPI.EXAM_INFORMATION_LIST, query.toMap(), QFNUAPI.INDEX);
        if (html.contains("未查询到数据")) return List.of();
        return examScheduleParser.parser(html);
    }
}
```

**Service 编写要点：**

- 构造函数接收 `QFNUContext`（运行时上下文）和所需的 `HtmlParser` 实例
- 使用 `Objects.requireNonNull` 校验参数
- 处理"未查询到数据"等特殊响应
- 提供默认参数的重载方法（如 `getExamSchedules()`）

#### 步骤 4：添加 API 端点

在 `core/QFNUAPI.java` 枚举中添加新端点：

```java
// 考试信息查询
EXAM_INFORMATION_LIST(BASE_URL.value + "/xsks/xsksap_list"),
```

端点命名规范：`模块_动作`，如 `EXAM_INFORMATION_LIST`、`GRADE_INQUIRY`。

#### 步骤 5：注册组件

在 `core/QFNUBuiltinModule.java` 中注册新的 Parser 和 Service：

```java
// 在 registerParsers() 中添加
registry.registerParser(ExamScheduleParser.class,
        resolver -> new ExamScheduleParser());

// 在 registerServices() 中添加
registry.registerService(ExamScheduleService.class,
        resolver -> new ExamScheduleService(
                resolver.context(),
                resolver.parser(ExamScheduleParser.class)
        ));
```

**注册要点：**

- Parser 通常无依赖，直接 `new` 即可
- 如果 Parser 依赖其他 Parser，通过 `resolver.parser(XxxParser.class)` 获取
- Service 通过 `resolver.context()` 获取上下文，`resolver.parser()` 获取 Parser

#### 步骤 6：编写测试

```java
// ExamScheduleParserTest.java (单元测试)
class ExamScheduleParserTest {
    private final ExamScheduleParser parser = new ExamScheduleParser();

    @Test
    void testParseValidHtml() {
        String html = Files.readString(
                Path.of("src/test/resources/exam_schedule_page.html"));
        List<ExamSchedule> schedules = parser.parser(html);
        assertFalse(schedules.isEmpty());
        assertEquals("高等数学", schedules.get(0).courseName());
    }

    @Test
    void testParseEmptyPage() {
        String html = "<html><body>未查询到数据</body></html>";
        List<ExamSchedule> schedules = parser.parser(html);
        assertTrue(schedules.isEmpty());
    }
}
```

测试资源 HTML 文件从真实教务页面导出（浏览器 → 查看页面源代码 → 保存）。

### 2.6 编码约定

#### 领域模型

- **使用 Java `record`** 定义领域模型（不可变性、自动生成样板代码）

```java
// ✅ 正确
public record StudentInfo(String name, String studentId, String academy,
                           String major, String className) {}

// ❌ 避免
public class StudentInfo {
    private String name;  // 可变，需要 getter/setter
    // ...
}
```

- **查询参数对象使用 Builder 模式**：

```java
GradeQuery query = GradeQuery.builder()
        .kksj("2025-2026-1")
        .kcmc("Java")
        .build();
```

#### 参数校验

- **所有 public API 使用 `Objects.requireNonNull`**：

```java
public class CourseService {
    public CourseService(QFNUContext context, HtmlParser<CourseTable> parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public CourseTable getCourseTable(Term term, int week) {
        Objects.requireNonNull(term, "term");
        // ...
    }
}
```

#### 日志规范

- **使用 SLF4J（`@Slf4j` 注解）**
- **敏感信息只打 debug 日志**（密码、验证码结果）
- **日志等级使用规则**：

| 等级 | 用途 | 示例 |
|------|------|------|
| `ERROR` | 可恢复的错误 | 网络请求失败、单个课程自动评教失败 |
| `WARN` | 非预期但可处理 | 验证码错误（会重试）、Session 即将过期 |
| `INFO` | 关键状态变更 | 登录成功、登录失败、Session 自动续期 |
| `DEBUG` | 开发调试（含敏感信息） | 请求 URL、验证码识别结果、Cookie 状态 |

#### 时间类型

- **统一使用 `LocalDate` / `LocalDateTime`**，不使用 `java.util.Date`
- **格式化在 `Util.java` 中集中管理**：

```java
// Util.java
public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
```

#### Parser 规范

- **实现 `HtmlParser<T>` 泛型接口**：

```java
// ✅ 正确：明确泛型类型
public class CourseParser implements HtmlParser<List<Course>> { }

// ❌ 避免：不指定泛型类型
public class CourseParser implements HtmlParser { }
```

- **解析异常时返回空集合而非 null**（列表类型的结果）
- **页面结构变化时抛出 `PageStructureException`**

#### 其他约定

- 使用 Lombok 消除样板代码（`@Slf4j`、`@RequiredArgsConstructor`）
- 枚举值使用 `UNDEFINED` 作为哨兵值，不使用 `null`
- Switch 表达式使用 `->` 箭头语法
- 静态内部类作为 Holder 实现懒加载单例

### 2.7 测试指南

#### 测试分类

| 类型 | 位置 | 标记 | 说明 |
|------|------|------|------|
| **单元测试** | `src/test/.../parser/` | 无 | 测试 Parser 解析逻辑，使用离线 HTML fixture，不需要网络 |
| **模型测试** | `src/test/.../model/` | 无 | 测试 Record 的 equals/hashCode、Builder、toMap 等方法 |
| **核心层测试** | `src/test/.../core/` | 无 | 测试 ComponentResolver、SessionInterceptor 等 |
| **集成测试** | `src/test/.../service/` | `@Tag("integration")` | 端到端测试 Service，需要真实凭据和网络 |
| **客户端测试** | `src/test/.../QFNUClientTest.java` | 无 | 测试 Builder 参数校验 |

#### 测试 Fixture 规范

- 单元测试 HTML fixture 从真实教务页面导出
- 导出方式：浏览器 → 登录教务系统 → 目标页面 → 查看页面源代码 → 保存到 `src/test/resources/`
- Fixture 文件命名：`<功能>_page.html`，如 `exam_schedule_page.html`
- **不要修改 fixture 中的 HTML**，保留原始结构以检测页面结构变化

#### 运行命令

```bash
# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest="CourseTableParseTest"

# 运行单个测试方法
mvn test -Dtest="CourseTableParseTest#testWeekdayMapping"

# 运行集成测试（需要 qfnuapi 凭据和网络）
mvn test -Dgroups="integration"

# 构建时跳过测试
mvn package -DskipTests
```

#### 编写新测试的规范

```java
class XxxParserTest {

    private final XxxParser parser = new XxxParser();

    @Test
    @DisplayName("正常 HTML 解析应返回正确的对象列表")
    void testParseValidHtml() throws Exception {
        // Arrange
        String html = Files.readString(
            Path.of("src/test/resources/xxx_page.html"));

        // Act
        List<Xxx> result = parser.parser(html);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("期望值", result.get(0).someField());
    }

    @Test
    @DisplayName("空数据页面应返回空列表")
    void testParseEmptyPage() {
        String html = "<html>未查询到数据</html>";
        List<Xxx> result = parser.parser(html);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 或空字符串输入不应抛异常")
    void testParseNullOrEmpty() {
        assertDoesNotThrow(() -> parser.parser(""));
        assertDoesNotThrow(() -> parser.parser(null));
    }
}
```

关键原则：
- 使用 `@DisplayName` 描述测试意图
- 测试空数据、null 输入、结构变化等边界情况
- 每个 Parser 至少 2 个测试用例（正常 + 空数据）

### 2.8 提交规范

#### Commit Message 格式

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <简短描述>

<详细说明（可选）>

<关联 Issue（可选）>
```

**Type 类型：**

| Type | 用途 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(evaluation): 完成学生评教自动化功能` |
| `fix` | Bug 修复 | `fix(parser): 修复课表解析中周次映射错误` |
| `refactor` | 代码重构 | `refactor(core): 重构组件注册与 DI 逻辑` |
| `docs` | 文档更新 | `docs: 更新 README 示例代码` |
| `test` | 测试相关 | `test(parser): 添加成绩解析器边界测试` |
| `chore` | 构建/工具 | `chore: 升级 OkHttp 到 4.12.0` |
| `style` | 代码风格 | `style: 统一缩进格式` |

**Scope 范围：**

| Scope | 关联包 |
|-------|--------|
| `core` | 核心框架层 |
| `parser` | 解析层 |
| `service` | 业务服务层 |
| `model` | 领域模型层 |
| `exception` | 异常体系 |
| `evaluation` | 评教功能 |

#### 分支策略

```
main          ← 稳定发布分支
  └── dev     ← 开发主分支
       └── feat/xxx  ← 功能分支
       └── fix/xxx   ← 修复分支
```

- `main` — 稳定版本，只接受来自 `dev` 的 Merge
- `dev` — 日常开发提交到此分支
- `feat/xxx` — 大功能使用单独分支开发后合并到 `dev`
- 小改动直接在 `dev` 上提交

### 2.9 发布流程

#### 版本号规则

遵循语义化版本（[SemVer](https://semver.org/lang/zh-CN/)）：

```
主版本号.次版本号.修订号
  0  .  0  .  1

MAJOR — 不兼容的 API 修改
MINOR — 向下兼容的新功能
PATCH — 向下兼容的 Bug 修复
```

当前版本：`0.0.1-SNAPSHOT`（开发中，尚未正式发布）。

#### 发布到本地 Maven 仓库

```bash
# 编译并安装到 ~/.m2/repository
mvn clean install -DskipTests
```

下游项目即可通过 Maven 坐标引入：

```xml
<dependency>
    <groupId>io.github.plaguewzk</groupId>
    <artifactId>qfnu-java-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 未来发布到 Maven Central（规划中）

1. 在 `pom.xml` 中配置 `distributionManagement` 和凭据
2. 注册 Sonatype JIRA 账号并申请 `io.github.plaguewzk` Group ID
3. 配置 GPG 签名
4. 执行 `mvn clean deploy`
5. 在 Sonatype Nexus 中 Release

> 正式发布流程将在项目稳定后详细记录。

---

## 附录

### 项目文件结构速查

```
io.github.plaguewzk.qfnujavaapi
├── QFNUClient.java              // 客户端入口 (Builder)
├── core/                        // 核心框架 (13 文件)
│   ├── QFNUAPI.java             // API 端点枚举
│   ├── QFNUExecutor.java        // HTTP 执行器
│   ├── QFNUCookieJar.java       // Cookie 管理
│   ├── SessionInterceptor.java  // Session 拦截
│   ├── QFNUContext.java         // 运行时上下文
│   ├── ComponentRegistry.java   // 组件注册表
│   ├── DefaultComponentResolver.java // DI 容器
│   ├── ComponentResolver.java   // 解析器接口
│   ├── ComponentProvider.java   // 组件工厂函数
│   ├── ParserRegistry.java      // Parser 注册接口
│   ├── ServiceRegistry.java     // Service 注册接口
│   ├── QFNUModule.java          // 扩展模块接口
│   └── QFNUBuiltinModule.java   // 内置模块
├── service/                     // 业务层 (7 文件)
│   ├── CaptchaService.java
│   ├── LoginService.java
│   ├── StudentService.java
│   ├── CourseService.java
│   ├── GradeService.java
│   ├── ExamScheduleService.java
│   └── NotificationService.java
├── parser/                      // 解析层 (16 文件)
│   ├── HtmlParser.java          // 解析接口
│   ├── ParserUtils.java         // 公共工具
│   └── impl/                    // 14 个解析实现
├── model/                       // 领域模型 (36 文件)
│   ├── course/   (9)            // 课表相关
│   ├── evaluation/ (12)         // 评教相关
│   ├── exam/     (3)            // 考试相关
│   ├── grade/    (7)            // 成绩相关
│   ├── notification/ (2)        // 通知相关
│   └── student/  (1)            // 学生信息
├── exception/                   // 异常体系 (13 文件)
└── util/
    └── Util.java                // 工具方法
```

### Maven 依赖一览

| 依赖 | GroupId | ArtifactId | 版本 | Scope |
|------|---------|------------|------|-------|
| Lombok | org.projectlombok | lombok | 1.18.42 | provided |
| OkHttp | com.squareup.okhttp3 | okhttp | 4.12.0 | compile |
| Jsoup | org.jsoup | jsoup | 1.17.2 | compile |
| Tess4J | net.sourceforge.tess4j | tess4j | 5.10.0 | compile |
| SLF4J | org.slf4j | slf4j-api | 2.0.12 | compile |
| Logback | ch.qos.logback | logback-classic | 1.5.22 | test |
| JUnit API | org.junit.jupiter | junit-jupiter-api | 5.10.2 | test |
| JUnit Engine | org.junit.jupiter | junit-jupiter-engine | 5.10.2 | test |
```
