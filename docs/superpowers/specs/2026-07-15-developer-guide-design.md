# 开发文档（DEVELOPER_GUIDE.md）设计规格

## 概述

为 QFNU-Java-API 项目创建一份完整详尽的开发文档 `docs/DEVELOPER_GUIDE.md`，兼顾 **SDK 使用者** 和 **外部贡献者** 两类读者。

## 目标文件

- 路径: `docs/DEVELOPER_GUIDE.md`
- 格式: Markdown
- 语言: 简体中文

## 文档结构

### 第一部分：使用者指南

| 章节 | 内容 |
|------|------|
| 1.1 简介与特性 | 项目定义、Badge 行、特性列表 |
| 1.2 环境要求与依赖引入 | JDK 17+、Maven 3.x、git clone + mvn install、Maven 坐标 |
| 1.3 配置说明 | config.qfnuapi.properties 模板、Builder.account() 传参 |
| 1.4 快速开始 | 7 个渐进示例：学生信息 → 通知 → 本周课表 → 学期课表 → 成绩 → 考试 → 评教 |
| 1.5 API 参考 | 6 个 Service 接口的方法签名表格 |
| 1.6 验证码自定义 | CaptchaService 接口、DefaultCaptchaService 流程、自定义示例 |
| 1.7 扩展模块开发 | QFNUModule 接口、注册 Parser/Service、依赖注入 |
| 1.8 常见问题 | Session 过期、验证码重试、日志级别、超时设置 |

### 第二部分：贡献者指南

| 章节 | 内容 |
|------|------|
| 2.1 项目架构深度解析 | 分层架构图（ASCII）、各层职责、包依赖规则 |
| 2.2 核心机制详解 | 组件注册与 DI、HTTP 执行链、Session 过期检测、验证码 OCR |
| 2.3 异常体系 | 完整异常树状图 + 触发场景 |
| 2.4 开发环境搭建 | JDK、Tesseract 语言包、IDE Lombok 配置 |
| 2.5 添加新功能流程 | 以考试安排为例的 6 步流程 |
| 2.6 编码约定 | Record 模型、null 检查、SLF4J 日志、时间类型、泛型 Parser |
| 2.7 测试指南 | 单元测试 vs 集成测试、fixture 规范、@Tag("integration")、运行命令 |
| 2.8 提交规范 | Conventional Commits 格式、分支策略 |
| 2.9 发布流程 | 版本号规则、mvn package、本地安装 / Maven Central |

## 写作风格

- 代码块使用 Java 语法高亮
- 关键概念使用表格和列表组织
- 架构部分使用 ASCII 文本图
- 方法签名使用 Javadoc 风格呈现
- 所有示例代码可直接运行

## 验收标准

- 文档覆盖全部规划章节
- 代码示例语法正确
- 架构描述与实际代码一致
- 链接可点击（类名引用等）
