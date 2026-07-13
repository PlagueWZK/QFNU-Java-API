# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

QFNU-Java-API 是曲阜师范大学（QFNU）教务管理系统的非官方 Java SDK，底层为强智科技教务平台。项目通过 HTTP + HTML 解析方式封装了登录、验证码识别、课表查询、成绩查询、考试安排查询、通知公告等功能。

## 构建与测试

```bash
# 编译项目
mvn compile

# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest="CourseTableParseTest"

# 运行单个测试方法
mvn test -Dtest="CourseTableParseTest#testWeekdayMapping"

# 打包（跳过测试）
mvn package -DskipTests

# 运行 CaptchaStrategyEvaluatorApp（验证码识别策略评估工具，有 main 方法）
mvn exec:java -Dexec.mainClass="io.github.plaguewzk.qfnujavaapi.service.impl.CaptchaStrategyEvaluatorApp"
```

- Java 17，Maven 构建（`pom.xml`）
- 使用 JUnit Jupiter 5.10.2 测试框架，所有测试均为纯单元测试，不连接真实教务系统
- Lombok 注解处理器已配置在 `maven-compiler-plugin` 中

## 本地开发配置

1. 将 `config.qfnuapi.properties.example` 复制为 `config.qfnuapi.properties`
2. 填入真实教务系统账号密码：
   ```properties
   qfnu.account=202xxxxxxx
   qfnu.password=your-password
   ```

`config.qfnuapi.properties` 已被 `.gitignore` 忽略。

## 架构核心

### 分层设计

```
QFNUClient (入口层，Builder 模式)
  ├── core/    核心框架层：HTTP执行、Cookie管理、Session拦截、组件注册/解析
  ├── service/ 业务服务层：组合 HTTP 调用 + Parser，暴露业务 API
  ├── parser/  解析层：HtmlParser<T> 接口，将 HTML 转换为领域模型
  └── model/   领域模型层：不可变 Java Record
```

### 组件注册与解析机制

这是项目最核心的扩展机制，理解它能快速定位代码：

1. **`ComponentRegistry`** — 同时实现 `ParserRegistry` 和 `ServiceRegistry`，内部用两个 `LinkedHashMap<Class<?>, ComponentProvider<?>>` 存储 parser 和 service 的懒加载工厂。
2. **`DefaultComponentResolver`** — 实现 `ComponentResolver`，直接持有 registry 并负责按需实例化和缓存组件（`ConcurrentHashMap`），构造即完整，无需额外的 bind 步骤。
3. **`QFNUModule`** 接口 — 下游项目可实现此接口来注册自定义 parser/service，通过 `QFNUClient.Builder.install(module)` 集成。

关键设计原则：**Parser 可以依赖其他 Parser，Service 可以依赖 Parser，但 Parser 不应依赖 Service**。依赖通过构造函数声明，由 `DefaultComponentResolver` 在实例化时自动注入。

### HTTP 执行链

```
QFNUExecutor (OkHttpClient 封装，提供 GET/POST/URL构建)
  └── SessionInterceptor (OkHttp Interceptor)
       └── 检测 Session 过期 → 同步调用登录 → 重试原请求
            └── 登录失败 → SessionRefreshException
  └── QFNUCookieJar (CookieJar 实现)
       └── ConcurrentHashMap 存储，线程安全，过期清理
```

### Session 过期检测逻辑

`SessionInterceptor` 判断响应是否为登录重定向：检查 response URL 是否指向登录页面（`previous` 参数或 `login` 路径），或 response body 是否包含登录表单标记。如果过期，**同步**（`synchronized` 块内）执行登录，然后重试原请求。

### 所有 API 端点

`QFNUAPI` 枚举集中管理所有端点 URL，基址为 `http://zhjw.qfnu.edu.cn/jsxsd/`。新增端点应在此枚举中添加。

### 验证码识别策略

`DefaultCaptchaService` 使用 Tesseract OCR（tess4j），图像预处理流程：
- 3x 双三次缩放
- 二值化（默认 fixed-170 阈值，支持 OTSU）
- 噪声去除 + 对比度归一化
- PSM 8 模式识别
- 结果验证正则：`^[0-9a-z]{4}$`

多策略通过 `StrategySpec` record 定义，`CaptchaStrategyEvaluatorApp` 可对联机验证码评估各策略成功率。

## 添加新功能的步骤

以添加"考试安排"功能为例的标准流程：

1. **`model/`** — 创建领域 Record（如 `ExamSchedule`、`ExamScheduleQuery`）
2. **`parser/impl/`** — 实现 `HtmlParser<T>` 接口的解析器
3. **`service/`** — 创建业务 Service，构造函数接收所需 Parser
4. **`core/QFNUAPI.java`** — 添加新的端点 URL（如需）
5. **`core/QFNUBuiltinModule`** — 在 `configure()` 中注册新组件的 Parser 和 Service
6. **测试** — 使用真实教务页面导出 HTML 作为测试 fixture，编写单元测试

## 异常体系

```
QFNUAPIException (RuntimeException)
├── AuthenticationException (含 InvalidCredentialsException、LoginFailedException、SessionRefreshException)
├── NetworkException
├── ParseException (含 PageStructureException、ParsingErrorException)
├── CaptchaException (含 CaptchaInitializationException、CaptchaRecognitionException)
├── InvalidParameterException
└── ServiceCreationException
```

用户凭证错误会抛 `InvalidCredentialsException`；验证码识别错误会触发自动重试（最多 20 次）；Session 过期会自动刷新。

## 编码约定

- 领域模型使用 Java `record`（不可变）
- 所有 public API 参数使用 `null` 检查和 `Objects.requireNonNull` 验证
- 日志使用 SLF4J（`@Slf4j`），敏感信息（密码、验证码结果）只打 debug 日志
- 时间字段统一使用 `LocalDate` / `LocalDateTime`，格式化在 `Util.java` 中集中管理
- Parser 返回泛型类型，通过 `HtmlParser<T>` 接口约束
