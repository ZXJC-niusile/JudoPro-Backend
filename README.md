# JudoPro-Backend · 柔道赛事信息检索与技战术智能分析后端系统

![Java](https://img.shields.io/badge/Java-11-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.5-6DB33F?logo=springboot&logoColor=white)
![Apache Lucene](https://img.shields.io/badge/Apache%20Lucene-8.11.1-005571?logo=apache&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql&logoColor=white)
![Multimodal AI](https://img.shields.io/badge/AI-Volcengine%20%2B%20Python%20CV-4F46E5)

> **大学生创新创业训练计划（大创）项目后端工程**
>
> 本项目基于上游《信息检索与搜索引擎》课程基础工程 [`ruoyu-chen/ir_demo`](https://github.com/ruoyu-chen/ir_demo) 与早期原型 [`ZXJC-niusile/ir_ijf`](https://github.com/ZXJC-niusile/ir_ijf) 进行了**全面深度二次开发与业务重构**：从最初单一的课程检索骨架，拓展升级为集 **国际柔道联合会（IJF）赛事数据自动化采集、多维度智能检索引擎、用户安全认证、多媒体资源库及“多模态大模型 + Python 视觉微服务”双引擎柔道技战术 AI 分析** 于一体的完整后端服务系统。
>
> **声明：本项目所涉赛事与运动员公开数据仅用于高校学术研究与创新训练，严禁用于任何商业用途。**

---

## 📖 项目概述

**JudoPro-Backend** 面向柔道运动训练辅助、运动员档案检索与赛事影像复盘场景构建。针对传统体育资料分散、跨维度查询困难以及赛事视频/图像缺乏自动化技战术解读等痛点，团队在上游课程框架基础上完成了以下核心演进：

1. **垂直领域重构**：构建面向 [国际柔道联合会（IJF）](https://www.ijf.org/) 的多优先级定向爬虫与柔道领域数据模型（体重级别、年龄组别、全球国家/大洲映射、聚光灯及赛事图集）。
2. **检索引擎全面升级**：基于 Apache Lucene 与 HanLP 分词器，研发涵盖精确分词、区间过滤、多条件布尔组合、编辑距离模糊纠错及智能优先排序的多维检索服务。
3. **业务全栈化扩展**：引入 MySQL + Spring Data JPA 持久化体系与 Spring Security + Session 双重鉴权机制，补齐完整的用户与个人媒体资产管理能力。
4. **多模态 AI 技战术分析**：打通火山引擎（Volcengine）多模态大模型与 Python 计算机视觉微服务，支持对柔道比赛图片与视频流进行动作识别、可视化标注回传与专业技战术文字复盘。

---

## 🏗 系统架构

```mermaid
flowchart TB
    Client["前端 Web / 客户端应用"] --> Interceptor["API 日志拦截器 & Spring Security 鉴权"]
    Interceptor --> QC["QueryController<br/>多维运动员检索接口"]
    Interceptor --> UC["UserController<br/>用户注册 / 登录 / 会话管理"]
    Interceptor --> FC["FileUploadController<br/>媒体管理与双引擎 AI 分析"]

    subgraph SearchEngine["信息采集与搜索引擎层"]
        Crawler["WebMagic IJF 爬虫<br/>(IjfCrawler / PriorityScheduler)"] --> Pipeline["LucenePipeline & JsonFilePipeline"]
        Pipeline --> LuceneIdx["Apache Lucene 8.11.1 倒排索引<br/>(HanLP 中文分词)"]
        QC --> IdxService["IdxService / SearchCriteria"]
        IdxService --> LuceneIdx
    end

    subgraph DataAndAI["持久化与多模态 AI 分析层"]
        UC --> MySQL[("MySQL 8.0<br/>users / user_files / ai_analysis")]
        FC --> LocalStorage[("本地多媒体存储<br/>uploads/images & videos")]
        FC --> MySQL
        FC --> Volcengine["火山引擎多模态大模型<br/>专业柔道技战术文字解析"]
        FC --> PythonCV["Python 视觉分析微服务<br/>二进制流姿态检测与视觉标注"]
    end
```

---

## 🛠 技术栈

| 类别 | 技术选型 | 版本 / 说明 |
| :--- | :--- | :--- |
| **开发语言** | Java | JDK 11 |
| **核心框架** | Spring Boot | 2.7.5（Web / Validation / DevTools） |
| **安全认证** | Spring Security | BCrypt 密码加密 + HttpSession / SecurityContext 双重认证 |
| **数据持久化** | Spring Data JPA + MySQL | Hibernate ORM（`ddl-auto=update`）+ MySQL Connector 8.0.33 |
| **全文检索引擎** | Apache Lucene | 8.11.1（Core / Queries / QueryParser / Highlighter） |
| **中文分词器** | HanLP | `portable-1.8.3` + `hanlp-lucene-plugin 1.1.7` |
| **数据采集爬虫** | WebMagic | 0.7.6（支持优先级队列调度与多 Pipeline 输出） |
| **HTTP 与多模态通信** | OkHttp3 + Retrofit2 | OkHttp 4.10.0（支持大文件二进制流传输与超时重试） |
| **AI 协同服务** | 火山引擎多模态 API + Python CV 微服务 | 技战术文本生成 + 图像/视频骨骼动作标注 |

---

## ✨ 二次开发核心功能模块

### 1. 🕷️ IJF 柔道数据定向采集引擎 (`crawler` & `model`)
- **三级优先级爬取调度**：基于 WebMagic `PriorityScheduler` 自动遍历国家/地区列表页、运动员索引页与个人详情页（`https://www.ijf.org/judoka`）。
- **结构化档案提取**：自动抽取运动员 ID、姓名、年龄、代表国家/地区及国旗图标、参赛公斤级，并聚合提取 **聚光灯写真（Under The Spotlights）** 与 **赛事抓拍（Photos）** 图集。
- **双通道落盘**：通过 `JsonFilePipeline` 归档原始 JSON 数据，同时经由 `LucenePipeline` 实时写入倒排索引，并支持从本地 `workspace` 一键重建全量索引。

### 2. 🔍 多维度与智能柔道检索引擎 (`index` & `QueryController`)
- **专业柔道领域建模**：
  - **年龄组别（`AgeGroup`）**：`CADET`（青少年组）、`JUNIOR`（青年组）、`SENIOR`（成年组）、`VETERAN`（大师/资深组）。
  - **标准体重级别（`WeightClass`）**：覆盖 `-60kg`、`-66kg`、`-73kg`、`-81kg`、`-90kg`、`-100kg`、`+100kg` 等国际标准级别。
  - **全球地理映射（`Continent` & `CountryContinentMapping`）**：内置六大洲与国家/IOC 代码映射表，支持动态扩展未归类国家集合（`others`）。
- **丰富的检索模式**：支持关键词分页检索、单维度精确筛选、年龄/体重数值区间查询、多条件组合检索（`SearchCriteria` 建造者模式）、基于编辑距离的模糊容错搜索（`fuzzy`）、高级复合检索（`advanced`）以及“精确优先 + 模糊兜底”的智能检索（`smart`）。

### 3. 👤 用户权限与会话管理体系 (`UserController` & `SecurityConfig`)
- 支持邮箱注册、**用户名 / 邮箱双模式登录** 与 BCrypt 强哈希密码校验。
- 采用 `HttpSession` 与 `Spring Security Authentication` 双重会话识别策略，兼顾前后端分离跨域联调与接口级权限控制。

### 4. 🤖 多媒体资产管理与双引擎 AI 技战术分析 (`FileUploadController` & `AIAnalysisService`)
- **全生命周期媒体管理**：支持单图、多图批量及大体积比赛视频上传（默认单文件上限 50MB、单次请求 100MB），提供文件列表分页、详情查看、下载次数统计、热门/最新资源排行及描述维护。
- **双引擎并行/协同 AI 分析工作流**：
  1. **外部多模态大模型（火山引擎）**：内置专业柔道技战术分析提示词（亦支持自定义 Prompt），对上传的比赛画面或视频进行投技/寝技识别、攻防态势评估与改进建议生成；
  2. **Python 视觉分析微服务**：通过二进制流（`/analyze_binary`）或 Base64 接口联动视觉检测服务，自动拉取带关键点/动作框标注的结果图片或视频并回存至后端与 MySQL `ai_analysis` 表。

---

## 📂 项目目录结构

```text
JudoPro-Backend
├── pom.xml                                        # Maven 依赖与构建配置
└── src
    ├── main
    │   ├── java/cn/edu/bistu/cs/ir
    │   │   ├── IrDemoApplication.java             # Spring Boot 应用启动入口
    │   │   ├── config/                            # 全局配置层
    │   │   │   ├── Config.java                    # 爬虫与索引工作目录参数配置
    │   │   │   ├── FileUploadConfig.java          # 多媒体上传限制与路径配置
    │   │   │   ├── SecurityConfig.java            # Spring Security 权限与会话配置
    │   │   │   ├── VolcengineConfig.java          # 火山引擎多模态大模型连接与重试配置
    │   │   │   └── WebMvcConfig.java              # CORS 跨域、静态资源映射与拦截器注册
    │   │   ├── controller/                        # RESTful 接口控制层
    │   │   │   ├── QueryController.java           # 柔道运动员多维检索与索引管理接口
    │   │   │   ├── UserController.java            # 用户注册、登录、会话与资料接口
    │   │   │   └── FileUploadController.java      # 图片/视频管理与双引擎 AI 分析接口
    │   │   ├── crawler/                           # 数据采集层
    │   │   │   ├── IjfCrawler.java                # IJF 国际柔道联合会页面解析处理器
    │   │   │   └── CrawlerService.java            # 爬虫生命周期与异步调度服务
    │   │   ├── dto/                               # 数据传输对象 (FileWithAnalysisDTO)
    │   │   ├── entity/                            # JPA 数据库实体 (UserFile, AIAnalysis)
    │   │   ├── index/                             # Lucene 搜索引擎核心层
    │   │   │   ├── IdxService.java                # 索引构建、多维/模糊/智能查询与重建服务
    │   │   │   └── LucenePipeline.java            # WebMagic -> Lucene 实时索引写入管道
    │   │   ├── interceptor/                       # 全局请求日志拦截器 (ApiLoggingInterceptor)
    │   │   ├── model/                             # 领域模型与枚举 (Player, PhotoEntity, AgeGroup, WeightClass, Continent, User 等)
    │   │   ├── repository/                        # Spring Data JPA 持久层接口
    │   │   ├── service/                           # 核心业务逻辑层 (AIAnalysisService, SearchCriteria, UserFileService, UserService)
    │   │   └── utils/                             # 通用工具类 (分页封装 PageResponse、统一响应 QueryResponse、文件/网络/年龄计算等)
    │   └── resources/
    │       ├── application.properties             # 核心服务、数据库与上传参数配置
    │       ├── schema.sql                         # 数据库表结构参考脚本
    │       └── logback-spring.xml                 # 分级日志配置
    └── test/java/cn/edu/bistu/cs/ir               # 组合查询、模糊检索、区间过滤与领域映射单元测试集
```

---

## 📡 RESTful API 接口概览

### 1. 运动员多维检索模块 (`/query`)

| 方法 | 路径 | 核心参数 | 功能说明 |
| :---: | :--- | :--- | :--- |
| `GET` | `/query/kw` | `kw`, `pageNo`, `pageSize` | 基于 HanLP 分词的关键词分页检索 |
| `GET` | `/query/smart` | `keyword`, `page`, `size` | **智能检索**（精确匹配优先 + 模糊匹配补充） |
| `GET` | `/query/fuzzy` | `fuzzyKeyword`, `similarity`, `page`, `size` | 基于相似度阈值的模糊/容错检索 |
| `GET` | `/query/advanced` | `keyword`, `fuzzyKeyword`, `ageGroup`, `weightClass`, `continent`, `country` 等 | 支持模糊与精确混排的高级复合检索 |
| `GET` | `/query/combined` | `keyword`, `ageGroup`, `minAge`, `maxAge`, `weightClass`, `minWeight`, `maxWeight`, `continent`, `country` | 多维度布尔组合精确过滤分页检索 |
| `GET` | `/query/ageGroup` | `ageGroup` (`CADET` / `JUNIOR` / `SENIOR` / `VETERAN`) | 按柔道年龄组别分页检索 |
| `GET` | `/query/ageRange` | `minAge`, `maxAge`, `pageNo`, `pageSize` | 按年龄数值区间分页检索 |
| `GET` | `/query/weightClass` | `weightClass` (`-60`, `-66`, `-73`, `-81`, `-90`, `-100`, `+100`) | 按国际标准公斤级分页检索 |
| `GET` | `/query/weightRange` | `minWeight`, `maxWeight`, `pageNo`, `pageSize` | 按体重数值范围分页检索 |
| `GET` | `/query/continent` / `/query/country` | `continent` / `country` | 按所属大洲或国家/地区分页检索 |
| `GET` | `/query/ageGroups` / `/query/weightClasses` / `/query/continents` / `/query/countries` | `continent`（仅 `/countries` 可选） | 获取前端筛选器所需的字典枚举元数据 |
| `GET` | `/query/others` / `/query/addOthers` | `continent`, `country` | 查询或动态追加指定大洲的扩展国家列表 |
| `POST` | `/query/rebuild-index` | 无 | 从本地 `workspace` 爬虫归档数据全量重建 Lucene 索引 |

### 2. 用户认证模块 (`/api/user`)

| 方法 | 路径 | 参数 / 请求体 | 功能说明 |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/user/register` | JSON: `username`, `email`, `password` | 新用户邮箱注册（BCrypt 加密存储） |
| `POST` | `/api/user/login` | JSON: `username`（支持用户名或邮箱）, `password` | 登录并建立 HttpSession 与 Security 认证上下文 |
| `GET` | `/api/user/me` | Session Cookie | 获取当前已登录用户信息 |
| `GET` | `/api/user/info/{userId}` | 路径参数 `userId` | 根据用户 ID 查询公开资料 |
| `POST` | `/api/user/logout` | Session Cookie | 注销当前会话并清理认证上下文 |

### 3. 多媒体与双引擎 AI 分析模块 (`/api/file`)

| 方法 | 路径 | 核心参数 | 功能说明 |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/file/analyze` | `file` (图片/视频), `prompt` (可选), `description` (可选) | **核心接口**：存储媒体并联动火山引擎大模型 + Python 视觉服务完成技战术文字与视觉标注分析 |
| `POST` | `/api/file/upload/image` | `file`, `description`, `category`, `is_analysis_requested` | 上传单张图片（支持按需触发条件式 AI 分析） |
| `POST` | `/api/file/upload/images` | `files` | 批量上传多张图片 |
| `POST` | `/api/file/upload/video` | `file`, `description`, `category`, `is_analysis_requested` | 上传比赛视频（支持按需触发条件式 AI 分析） |
| `GET` | `/api/file/list` | `page`, `size`, `type` | 分页获取当前用户的媒体文件及关联 AI 分析结果 |
| `GET` | `/api/file/detail/{fileId}` | 路径参数 `fileId` | 查询单个文件元数据与详细 AI 分析报告 |
| `GET` | `/api/file/download/{fileId}` | 路径参数 `fileId` | 下载媒体文件并自动累加下载计数 |
| `GET` | `/api/file/statistics` / `/recent` / `/popular` | 分页或数量参数 | 获取用户媒体统计概览、最新上传列表、高下载量热门文件 |
| `PUT` | `/api/file/description/{fileId}` | `fileId`, `description` | 更新媒体文件备注说明 |
| `DELETE` | `/api/file/delete/{fileId}` / `/api/file/delete` | `fileId` 或批量 ID | 删除单个或批量清理用户媒体文件 |
