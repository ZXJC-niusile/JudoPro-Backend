# JudoPro-Backend

> 本项目在 [`ZXJC-niusile/ir_ijf`](https://github.com/ZXJC-niusile/ir_ijf) 和 `ir_demo` 的基础上实现了对 IJF（国际柔道联合会）数据的适配。
> 
> **注意：本项目仅用于学习和研究使用！**

## 📖 项目简介

JudoPro-Backend 是一个基于 **Spring Boot** 的信息检索与搜索引擎系统后端。该项目主要用于爬取、索引和检索 Judo（柔道）运动员的相关数据，提供了多维度的查询接口，并集成了用户认证、文件上传及 AI 智能分析功能。

## 🛠 技术栈

- **开发语言**：Java 11
- **核心框架**：Spring Boot 2.7.5
- **持久层机制**：Spring Data JPA + MySQL 8.x
- **安全认证**：Spring Security (Session 机制)
- **搜索引擎**：Apache Lucene 8.11.1
- **中文分词**：HanLP (portable-1.8.3 / hanlp-lucene-plugin)
- **网络爬虫**：WebMagic 0.7.6
- **网络请求**：OkHttp + Retrofit 2

## ✨ 核心功能模块

### 1. 🔍 信息检索与搜索引擎 (Lucene)
- **多维度运动员检索**：支持按照关键词、年龄组别（CADET、JUNIOR、SENIOR、VETERAN）、体重级别（-60kg、-66kg 等）、大洲、国家、年龄范围和体重范围等进行精确与分页查询。
- **高级综合查询**：支持多条件的组合查询以及模糊搜索。
- **分词引擎**：集成 HanLP 实现高效准确的中文语义分词及索引构建。

### 2. 🕷️ 数据采集爬虫 (WebMagic)
- 适配国际柔道联合会 (IJF) 数据结构。
- 自动化抓取运动员基础信息、赛事照片等数据并保存到本地以供 Lucene 构建索引。

### 3. 👤 用户权限管理
- 基于 Spring Security 提供的用户注册、登录、登出以及会话管理功能。
- 支持基于用户名或邮箱的登录体系。

### 4. 📁 文件与 AI 分析
- 支持大文件上传（最大请求 100MB，最大文件 50MB）。
- 集成火山数字（Volcengine）提供的 AI 接口对数据/文件进行智能分析处理。

## 🚀 快速启动

### 1. 环境准备
- **Java 11** 及以上
- **Maven** 3.6+
- **MySQL 8.0** 及以上服务

### 2. 初始化数据库
新建名为 `JudoPro` 的 MySQL 数据库（对应 `application.properties` 中配置的部分）：
```sql
CREATE DATABASE JudoPro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
> 系统内已配置 `spring.jpa.hibernate.ddl-auto=update`，启动应用后将自动生成或更新数据表结构。

### 3. 修改配置
请在 `src/main/resources/application.properties` 文件中修改您的环境配置。
主要修改项：
```properties
# 数据库连接
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/JudoPro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password

# 文件存储、索引、爬虫目录配置 (请确保应用有权限读写该路径)
irdemo.dir.home = workspace
```

### 4. 运行与构建
- **编译 & 打包**：
  ```bash
  mvn clean package -DskipTests
  ```
- **启动服务**：
  可通过 IDE 正常启动 `IrDemoApplication.java` 或通过命令行：
  ```bash
  java -jar target/ir_demo-0.1.0-SNAPSHOT.jar
  ```

## 📂 项目结构描述

```text
cn.edu.bistu.cs.ir
 ├── config/        # 配置文件 (安全、WebMvc、文件上传、Volcengine等)
 ├── controller/    # 接口控制层 (多维检索、用户管理、文件上传)
 ├── crawler/       # 爬虫服务组件 (IJF定制爬虫)
 ├── dto/           # 数据传输对象
 ├── entity/        # JPA 数据实体 (数据库映射)
 ├── index/         # Lucene 索引构建与检索核心代码
 ├── model/         # 枚举与模型类 (大洲, 国家, 体重, 年龄等枚举)
 ├── repository/    # 数据库持久层接口 (Spring Data JPA)
 ├── service/       # 业务逻辑服务
 └── utils/         # 基础工具类整合 (分页、网络、文件、JSON处理等)
```

## 📝 接口概览 (Query API)

常用的检索接口 (`/query` 下)：
- `/query/kw`：基于关键词的分页查询
- `/query/ageGroup`：基于年龄组别（如 CADET, SENIOR 等）的查询
- `/query/weightClass`：基于体重级别（如 -60, +100 等）的查询
- `/query/country` / `/query/continent`：基于国家或大洲的查询
- `/query/combined`：多维组合条件查询
- `/query/fuzzy`：模糊搜索

*请结合具体的业务接口文档或查看 `QueryController.java` 以获得完整的参数细节。*
