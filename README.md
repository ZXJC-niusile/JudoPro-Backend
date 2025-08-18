# JudoPro 柔道运动员信息检索系统

本工程在 [ZXJC-niusile/ir_ijf](https://github.com/ZXJC-niusile/ir_ijf) 和 [ir_demo](https://github.com/ruoyu-chen/ir_demo) 的基础上实现了对 [IJF](https://www.ijf.org/) 的适配

本项目是一个基于Spring Boot + Lucene + WebMagic + MySQL的柔道运动员信息检索系统，实现了从国际柔道联盟官网爬取运动员数据、建立Lucene索引进行高效检索、MySQL数据库用户管理等功能。
**仅用于学习和研究使用！**

## 核心功能

### 🔍 多维度检索系统
- **关键词检索**：支持运动员姓名、国家的精确关键词检索
- **分类检索**：年龄组别、体重级别、地区检索
- **范围检索**：年龄范围、体重范围检索
- **模糊匹配**：支持拼写错误和相似词的模糊匹配
- **高级搜索**：多字段组合检索，支持复杂查询条件
- **智能搜索**：结合精确匹配和模糊匹配的智能检索

### 🕷️ 数据爬取与存储
- **WebMagic爬虫**：从国际柔道联盟官网爬取运动员数据
- **Lucene索引**：运动员数据的高效全文检索索引存储
- **MySQL存储**：用户管理数据的结构化存储

### 👤 用户管理系统
- **邮箱注册登录**：基于邮箱+密码的安全认证
- **密码加密**：使用Spring Security进行密码加密存储
- **用户信息管理**：支持用户资料管理和头像设置

### 📁 用户文件管理 ⭐ 新增
- **文件上传**：支持图片、视频、文档等多种文件类型上传
- **文件存储**：安全的文件存储和访问控制
- **文件管理**：文件列表查看、搜索、筛选和分页
- **文件操作**：文件下载、删除（软删除）和统计
- **用户隔离**：确保用户只能管理自己的文件
- **文件统计**：提供文件数量、大小、下载次数等统计信息



## 技术栈

**开发语言**：Java 11  
**核心框架**：Spring Boot 2.x  
**搜索引擎**：Apache Lucene 8.11.1  
**爬虫框架**：WebMagic 0.7.6  
**数据库**：MySQL 8.0  
**ORM框架**：Spring Data JPA  
**安全框架**：Spring Security  
**分词工具**：HanLP portable-1.8.3  
**构建工具**：Maven

## 快速开始

### 1. 环境要求
- Java 11+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
```sql
-- 创建数据库
CREATE DATABASE judopro DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行schema.sql创建表结构
-- 执行data.sql插入测试数据（可选）
```

### 3. 应用配置
修改 `application.properties` 中的数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/judopro
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 运行应用
```bash
mvn spring-boot:run
```

### 5. 访问接口
- 检索接口：http://localhost:8080/query/
- 用户接口：http://localhost:8080/user/

## 主要特性

### 🎯 精准检索
- 支持多种检索方式，满足不同查询需求
- 智能权重排序，优先返回最相关结果
- 完整的分页支持和参数验证

### 🔒 安全可靠
- Spring Security安全框架保护
- 密码加密存储，防止明文泄露
- 统一的错误处理和异常管理

### 📈 高性能
- Lucene高效全文检索
- 数据库索引优化
- 分页查询性能优化

### 🔧 易扩展
- 模块化设计，便于功能扩展
- 统一的API接口规范
- 完整的文档和代码注释