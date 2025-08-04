# 柔道运动员信息检索系统 - 项目总结

## 项目概述

本项目是一个基于Spring Boot + Lucene + WebMagic的柔道运动员信息检索系统，实现了从国际柔道联盟官网爬取运动员数据、建立Lucene索引、提供多种检索方式等功能。

## 技术栈

- **后端框架**: Spring Boot
- **搜索引擎**: Apache Lucene
- **爬虫框架**: WebMagic
- **数据存储**: Lucene索引
- **构建工具**: Maven

## 核心功能

### 1. 数据爬取功能
- 爬取国际柔道联盟官网运动员数据
- 支持增量爬取和全量爬取
- 数据清洗和格式化处理

### 2. 索引构建功能
- 基于Lucene的全文索引构建
- 支持多字段索引（姓名、年龄、体重、国家等）
- 数值字段索引（年龄、体重范围查询）

### 3. 检索功能

#### 3.1 基础检索
- **关键词检索**: `GET /query/kw?kw=zhang&pageNo=1&pageSize=10`
- **年龄组别检索**: `GET /query/ageGroup?ageGroup=SENIOR&pageNo=1&pageSize=10`
- **体重级别检索**: `GET /query/weightClass?weightClass=-73&pageNo=1&pageSize=10`
- **大洲检索**: `GET /query/continent?continent=ASIA&pageNo=1&pageSize=10`
- **国家检索**: `GET /query/country?country=China&pageNo=1&pageSize=10`

#### 3.2 范围检索
- **年龄范围检索**: `GET /query/ageRange?minAge=20&maxAge=30&pageNo=1&pageSize=10`
- **体重范围检索**: `GET /query/weightRange?minWeight=60.0&maxWeight=80.0&pageNo=1&pageSize=10`

#### 3.3 模糊匹配检索 ⭐ 新增
- **模糊匹配检索**: `GET /query/fuzzy?fuzzyKeyword=zhang&similarity=0.8&page=0&size=10`
  - 支持拼写错误的模糊匹配
  - 支持通配符匹配(*和?)
  - 支持前缀匹配
  - 支持相似度阈值控制

#### 3.4 高级搜索 ⭐ 新增
- **高级搜索**: `GET /query/advanced?keyword=zhang&fuzzyKeyword=li&similarity=0.7&ageGroup=SENIOR&minAge=20&maxAge=30&weightClass=LIGHTWEIGHT&minWeight=60.0&maxWeight=80.0&continent=ASIA&country=China&page=0&size=10`
  - 支持多字段任意组合
  - 支持布尔逻辑操作
  - 支持参数验证
  - 支持复杂查询条件

#### 3.5 智能搜索 ⭐ 新增
- **智能搜索**: `GET /query/smart?keyword=zhang&page=0&size=10`
  - 自动结合精确匹配和模糊匹配
  - 智能权重排序
  - 优先返回最相关结果

#### 3.6 组合检索
- **组合条件检索**: `GET /query/combined?keyword=zhang&ageGroup=SENIOR&weightClass=-73&continent=ASIA&pageNo=1&pageSize=10`

### 4. 数据获取功能
- **获取大洲列表**: `GET /query/continents`
- **获取国家列表**: `GET /query/countries?continent=ASIA`
- **获取年龄组别列表**: `GET /query/ageGroups`
- **获取体重级别列表**: `GET /query/weightClasses`
- **获取Others国家列表**: `GET /query/others?continent=ASIA`
- **动态添加Others国家**: `GET /query/addOthers?country=NewCountry&continent=ASIA`

### 5. Others功能
- 为各大洲动态添加"others"国家
- 大洲检索时自动包含该大洲下的所有国家（包括others）
- 支持Others国家的管理

## 核心类说明

### 1. 模型类 (model包)
- **Player**: 运动员实体类
- **AgeGroup**: 年龄组别枚举（CADET, JUNIOR, SENIOR, VETERAN）
- **WeightClass**: 体重级别枚举（EXTRA_LIGHTWEIGHT, LIGHTWEIGHT, MIDDLEWEIGHT等）
- **Continent**: 大洲枚举（ASIA, EUROPE, AFRICA等）
- **CountryContinentMapping**: 国家与大洲映射关系类

### 2. 服务类 (service包)
- **SearchCriteria**: 检索条件封装类，支持构建器模式
  - 支持关键词、模糊关键词、相似度
  - 支持年龄组别、年龄范围
  - 支持体重级别、体重范围
  - 支持大洲、国家
  - 提供参数验证方法

### 3. 索引服务 (index包)
- **IdxService**: Lucene索引服务类
  - 基础检索方法（关键词、年龄组别、体重级别等）
  - 范围检索方法（年龄范围、体重范围）
  - 模糊匹配检索方法（FuzzyQuery, WildcardQuery, PrefixQuery）
  - 高级搜索方法（多字段组合检索）
  - 智能搜索方法（权重排序）
  - 组合检索方法

### 4. 控制器 (controller包)
- **QueryController**: 检索接口控制器
  - 提供所有检索接口
  - 统一响应格式（QueryResponse）
  - 参数验证和错误处理
  - 分页支持

### 5. 工具类 (utils包)
- **PageResponse**: 分页响应封装类
- **QueryResponse**: 统一响应格式类
- **JsonUtils**: JSON处理工具类

## 接口特点

### 1. 统一响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "data": [...],
    "pageInfo": {
      "pageNo": 1,
      "pageSize": 10,
      "total": 100,
      "totalPages": 10,
      "hasPrevious": false,
      "hasNext": true
    }
  }
}
```

### 2. 分页支持
- 所有检索接口都支持分页
- 分页参数：pageNo（页码，从1开始）、pageSize（每页大小，默认10，最大100）
- 返回完整的分页元数据

### 3. 参数验证
- 完整的参数验证机制
- 友好的错误提示信息
- 统一的错误处理

### 4. 性能优化
- Lucene层面分页查询
- 索引优化
- 缓存策略

## 新增功能亮点

### 1. 模糊匹配检索
- **多种模糊匹配方式**: FuzzyQuery（拼写错误）、WildcardQuery（通配符）、PrefixQuery（前缀）
- **相似度控制**: 支持设置相似度阈值（0.0-1.0）
- **智能权重**: 不同匹配方式的权重调整

### 2. 高级搜索
- **多字段组合**: 支持任意字段的组合检索
- **布尔逻辑**: 支持AND、OR、NOT等逻辑操作
- **复杂查询**: 支持复杂的查询条件组合

### 3. 智能搜索
- **智能排序**: 精确匹配 > 短语匹配 > 前缀匹配 > 模糊匹配 > 通配符匹配
- **权重系统**: 不同查询类型的权重设置
- **用户体验**: 优先返回最相关的结果

### 4. Others功能
- **动态管理**: 支持动态添加国家到Others集合
- **智能分类**: 自动处理未明确列出的国家
- **检索包含**: 大洲检索时自动包含Others国家

## 前端集成建议

### 1. 检索界面设计
- **搜索框**: 保留原有的关键词搜索功能
- **筛选按钮**: 添加年龄组别、体重级别、大洲、国家等筛选按钮
- **范围选择器**: 添加年龄范围和体重范围的选择器
- **高级选项**: 添加模糊匹配和相似度控制选项
- **组合检索**: 支持搜索框+筛选按钮的组合检索

### 2. 数据获取
- **初始化数据**: 页面加载时获取大洲、年龄组别、体重级别等基础数据
- **动态加载**: 根据选择的大洲动态加载对应的国家列表
- **Others处理**: 支持Others国家的显示和管理

### 3. 用户体验
- **实时搜索**: 支持输入时实时搜索
- **结果高亮**: 在搜索结果中高亮匹配的关键词
- **分页导航**: 提供完整的分页导航功能
- **结果统计**: 显示检索结果的总数和当前页信息

## 测试覆盖

### 1. 单元测试
- **SearchCriteriaTest**: 检索条件对象测试
- **SearchCriteriaRangeTest**: 范围检索功能测试
- **SearchCriteriaFuzzyTest**: 模糊匹配功能测试
- **CountryContinentMappingTest**: 国家映射功能测试

### 2. 集成测试
- **QueryControllerCombinedTest**: 组合检索接口测试

## 部署说明

### 1. 环境要求
- JDK 8+
- Maven 3.6+
- 内存: 2GB+

### 2. 启动步骤
1. 克隆项目到本地
2. 配置application.properties
3. 运行爬虫获取数据
4. 构建Lucene索引
5. 启动Spring Boot应用

### 3. 配置说明
- 索引目录配置
- 爬虫配置
- 日志配置

## 总结

本项目实现了一个功能完整的柔道运动员信息检索系统，从数据爬取到索引构建，从基础检索到高级搜索，提供了丰富的检索功能和良好的用户体验。

**主要特色**:
1. **多维度检索**: 支持关键词、年龄、体重、地区等多维度检索
2. **模糊匹配**: 支持多种模糊匹配方式，提高检索准确性
3. **高级搜索**: 支持复杂查询条件组合
4. **智能排序**: 基于相关性的智能结果排序
5. **Others功能**: 灵活处理未明确分类的数据
6. **统一接口**: 提供统一的API接口和响应格式

**技术亮点**:
1. **Lucene索引**: 高效的全文检索
2. **构建器模式**: 优雅的参数封装
3. **分页优化**: Lucene层面分页查询
4. **参数验证**: 完整的参数验证机制
5. **错误处理**: 统一的异常处理

该系统为前端开发提供了完整的接口支持，可以轻松实现各种检索界面和功能。 