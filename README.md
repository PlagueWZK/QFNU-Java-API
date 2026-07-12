# QFNU Java API (Unofficial)

> 🚧 **开发中 (Work in Progress)**
>
> 曲阜师范大学教务系统（强智科技）的非官方 Java SDK。

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![OkHttp](https://img.shields.io/badge/OkHttp-4.x-green)](https://square.github.io/okhttp/)
[![Jsoup](https://img.shields.io/badge/Jsoup-1.15%2B-blue)](https://jsoup.org/)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)

## 简介

通过 HTTP + HTML 解析封装了教务系统的登录、课表、成绩、考试、通知、评教等功能，提供类型安全的 Java API。

## 特性

* Builder 模式构建客户端，通过 `client.service(XxxService.class)` 按需获取服务。
* 自动登录与 Session 续期，过期时后台静默重新登录，上层业务无感。
* 内置验证码 OCR（Tesseract），支持自定义实现。
* 课表查询（当周 + 学期理论课表）、成绩查询、考试安排、通知公告。
* 学生评教：自动评分（最接近满分 / 最接近 90 分两种策略）、一键评完、最终提交。
* 基于 Jsoup 的独立解析层，HTML → Java Record。
* 通过 `QFNUModule` 扩展自定义 Parser / Service。

## 技术栈

* **语言**: Java 17
* **网络**: OkHttp3
* **解析**: Jsoup
* **OCR**: Tess4J (Tesseract)
* **工具**: Lombok, SLF4J
* **构建**: Maven

## 依赖引入

```powershell
git clone https://github.com/PlagueWZK/QFNU-Java-API.git
cd QFNU-Java-API
mvn -DskipTests install
```

执行后 Maven 会把 jar 和 pom 安装到本地仓库。下游项目引入：

```xml
<dependency>
    <groupId>io.github.plaguewzk</groupId>
    <artifactId>qfnu-java-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 快速开始

### 获取学生信息

```java
QFNUClient client = new QFNUClient.Builder()
        .account("学号", "密码")
        .build();

StudentInfo info = client.service(StudentService.class).getStudentInfo();
System.out.println(info.name() + " - " + info.academy());
```

### 获取通知公告

```java
List<Notification> list = client.service(NotificationService.class).getList();
list.forEach(item -> System.out.println(item.title() + " - " + item.publisher()));
```

### 获取本周课表

```java
WeeklySchedule schedule = client.service(CourseService.class)
        .getCurrentWeeklyScheduleFromMainPage();
System.out.println("第 " + schedule.currentWeek() + " 周，共 " + schedule.courseList().size() + " 门课");
```

### 获取学期理论课表

```java
CourseTable table = client.service(CourseService.class)
        .getCourseTable(Term.current(), 1);
table.courses().forEach(c ->
    System.out.println(c.weekday() + " " + c.courseName() + " " + c.section() + " " + c.location()));
```

### 学生评教

```java
StudentService svc = client.service(StudentService.class);

List<EvaluationEntry> entries = svc.getEvaluationList();
List<EvaluationCourse> courses = svc.getEvaluationCourses(entries.get(0));

// 一键评完所有课程（前 40% 高分，剩余接近 90）
List<EvaluationResult> results = svc.autoEvaluateAll(entries.get(0));
results.forEach(System.out::println);

// 检查后手动最终提交
svc.finalSubmit(entries.get(0));
```

### 注册自定义扩展

```java
public final class CustomModule implements QFNUModule {
    @Override
    public void configure(ParserRegistry parsers, ServiceRegistry services) {
        parsers.registerParser(CustomParser.class, resolver -> new CustomParser());
        services.registerService(CustomService.class,
                resolver -> new CustomService(resolver.executor(), resolver.parser(CustomParser.class)));
    }
}

QFNUClient client = new QFNUClient.Builder()
        .account("学号", "密码")
        .install(new CustomModule())
        .build();
```

## 项目结构

```Plaintext
io.github.plaguewzk.qfnujavaapi
├── QFNUClient.java          // 客户端入口
├── core                     // 核心框架
│   ├── QFNUAPI.java         // 接口常量
│   ├── QFNUExecutor.java    // HTTP 执行器
│   ├── ParserFactory.java   // Parser 工厂
│   ├── ServiceFactory.java  // Service 工厂
│   ├── SessionInterceptor.java // 会话拦截
│   └── QFNUCookieJar.java   // Cookie 管理
├── model                    // 数据模型
│   ├── course               // 课程 (Course, CourseTable, Term, Weekday...)
│   ├── evaluation           // 评教 (EvaluationEntry, EvaluationFormData, EvaluationSubmission...)
│   ├── exam                 // 考试 (ExamSchedule...)
│   ├── grade                // 成绩 (CourseGrade, GradeQuery...)
│   ├── notification         // 通知 (Notification...)
│   └── student              // 学生 (StudentInfo)
├── parser                   // 解析层
│   ├── HtmlParser.java      // 解析接口
│   ├── ParserUtils.java     // 公共工具
│   └── impl                 // 各页面解析实现
├── service                  // 业务层
│   ├── StudentService.java  // 学生 + 评教
│   ├── CourseService.java   // 课表
│   ├── GradeService.java    // 成绩
│   ├── ExamScheduleService.java // 考试
│   ├── NotificationService.java // 通知
│   └── LoginService.java    // 登录
├── util
│   └── Util.java
└── exception                // 异常体系
```

## 待办

- [x] 登录与 Session 续期
- [x] 学生信息、课表、成绩、考试、通知
- [x] 学生评教自动化
- [ ] 培养方案解析
- [ ] 选课功能

## 开源协议

Apache License 2.0，详见 [LICENSE](./LICENSE)。

## 免责声明

仅供学习交流使用。请勿高频请求或用于商业用途。使用本工具产生的后果由使用者自行承担。

## 作者

**PlagueWZK** 
- [GitHub](https://github.com/PlagueWZK)
- 邮箱: 2849996270@qq.com
