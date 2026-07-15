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
