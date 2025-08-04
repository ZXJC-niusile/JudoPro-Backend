# 柔道运动员信息检索系统 - 组合检索功能说明

## 概述

本系统基于Spring Boot + Lucene + WebMagic技术栈，实现了柔道运动员信息的爬取、索引构建和多维度组合检索功能。系统支持多种检索方式，包括精确检索、模糊匹配检索、高级搜索和智能搜索。

## 主要功能

### 1. 基础检索功能
- **关键词检索**: 支持运动员姓名、国家的精确关键词检索
- **分页查询**: 所有检索接口都支持分页，提供完整的分页元数据

### 2. 多级检索功能
- **年龄组别检索**: 支持青少年(CADET)、青年(JUNIOR)、成年(SENIOR)、资深(VETERAN)四个年龄组别
- **体重级别检索**: 支持轻量级、中量级、重量级等多个体重级别
- **地区检索**: 支持大洲检索和国家检索，实现多级地区检索

### 3. 范围检索功能
- **年龄范围检索**: 支持指定最小年龄和最大年龄的范围检索
- **体重范围检索**: 支持指定最小体重和最大体重的范围检索

### 4. 模糊匹配检索功能 ⭐ 新增
- **模糊查询**: 支持拼写错误和相似词的模糊匹配
- **通配符查询**: 支持*和?通配符的模糊匹配
- **前缀查询**: 支持前缀匹配的模糊检索
- **相似度控制**: 支持设置相似度阈值(0.0-1.0)来控制匹配精度

### 5. 高级搜索功能 ⭐ 新增
- **多字段组合**: 支持关键词、模糊关键词、年龄组别、年龄范围、体重级别、体重范围、大洲、国家等多个字段的组合检索
- **布尔逻辑**: 支持AND、OR、NOT等布尔逻辑操作
- **权重控制**: 支持不同查询条件的权重调整

### 6. 智能搜索功能 ⭐ 新增
- **智能排序**: 优先返回精确匹配结果，然后返回模糊匹配结果
- **多级权重**: 精确匹配(3.0) > 短语匹配(2.5) > 前缀匹配(2.0) > 模糊匹配(1.0) > 通配符匹配(0.5)

### 7. Others功能
- **动态添加**: 支持为各大洲动态添加"others"国家
- **组合查询**: 检索大洲时自动包含该大洲下的所有国家(包括others)

## API接口说明

### 基础检索接口

#### 1. 关键词检索
```
GET /api/query?keyword=zhang&page=0&size=10
```

#### 2. 年龄组别检索
```
GET /api/query/age-group?ageGroup=SENIOR&page=0&size=10
```

#### 3. 体重级别检索
```
GET /api/query/weight-class?weightClass=LIGHTWEIGHT&page=0&size=10
```

#### 4. 大洲检索
```
GET /api/query/continent?continent=ASIA&page=0&size=10
```

#### 5. 国家检索
```
GET /api/query/country?country=China&page=0&size=10
```

### 范围检索接口

#### 1. 年龄范围检索
```
GET /api/query/age-range?minAge=20&maxAge=30&page=0&size=10
```

#### 2. 体重范围检索
```
GET /api/query/weight-range?minWeight=60.0&maxWeight=80.0&page=0&size=10
```

### 组合检索接口

#### 1. 组合条件检索
```
GET /api/query/combined?keyword=zhang&ageGroup=SENIOR&weightClass=LIGHTWEIGHT&continent=ASIA&page=0&size=10
```

### 模糊匹配检索接口 ⭐ 新增

#### 1. 模糊匹配检索
```
GET /api/query/fuzzy?fuzzyKeyword=zhang&similarity=0.8&page=0&size=10
```

**参数说明:**
- `fuzzyKeyword`: 模糊关键词(必填)
- `similarity`: 相似度阈值，0.0-1.0(可选)
- `page`: 页码，从0开始(可选，默认0)
- `size`: 每页大小(可选，默认10)

**功能特点:**
- 支持拼写错误的模糊匹配
- 支持通配符匹配(*和?)
- 支持前缀匹配
- 支持相似度阈值控制

### 高级搜索接口 ⭐ 新增

#### 1. 高级搜索
```
GET /api/query/advanced?keyword=zhang&fuzzyKeyword=li&similarity=0.7&ageGroup=SENIOR&minAge=20&maxAge=30&weightClass=LIGHTWEIGHT&minWeight=60.0&maxWeight=80.0&continent=ASIA&country=China&page=0&size=10
```

**参数说明:**
- `keyword`: 精确关键词(可选)
- `fuzzyKeyword`: 模糊关键词(可选)
- `similarity`: 相似度阈值(可选)
- `ageGroup`: 年龄组别(可选)
- `minAge`: 最小年龄(可选)
- `maxAge`: 最大年龄(可选)
- `weightClass`: 体重级别(可选)
- `minWeight`: 最小体重(可选)
- `maxWeight`: 最大体重(可选)
- `continent`: 大洲(可选)
- `country`: 国家(可选)
- `page`: 页码(可选，默认0)
- `size`: 每页大小(可选，默认10)

**功能特点:**
- 支持多字段任意组合
- 支持布尔逻辑操作
- 支持参数验证
- 支持复杂查询条件

### 智能搜索接口 ⭐ 新增

#### 1. 智能搜索
```
GET /api/query/smart?keyword=zhang&page=0&size=10
```

**参数说明:**
- `keyword`: 搜索关键词(必填)
- `page`: 页码(可选，默认0)
- `size`: 每页大小(可选，默认10)

**功能特点:**
- 自动结合精确匹配和模糊匹配
- 智能权重排序
- 优先返回最相关结果

### Others功能接口

#### 1. 获取大洲列表
```
GET /api/query/continents
```

#### 2. 获取国家列表
```
GET /api/query/countries?continent=ASIA
```

#### 3. 获取Others国家列表
```
GET /api/query/others?continent=ASIA
```

#### 4. 动态添加Others国家
```
POST /api/query/others/add?country=NewCountry&continent=ASIA
```

## 使用示例

### 示例1: 模糊匹配检索
```bash
# 搜索包含"zhang"的运动员，相似度阈值0.8
curl "http://localhost:8080/api/query/fuzzy?fuzzyKeyword=zhang&similarity=0.8&page=0&size=10"
```

### 示例2: 高级搜索 - 亚洲成年组轻量级运动员
```bash
# 搜索亚洲成年组轻量级运动员，年龄20-30岁，体重60-80公斤
curl "http://localhost:8080/api/query/advanced?ageGroup=SENIOR&weightClass=LIGHTWEIGHT&continent=ASIA&minAge=20&maxAge=30&minWeight=60.0&maxWeight=80.0&page=0&size=10"
```

### 示例3: 智能搜索
```bash
# 智能搜索"zhang"，自动结合精确匹配和模糊匹配
curl "http://localhost:8080/api/query/smart?keyword=zhang&page=0&size=10"
```

### 示例4: 组合检索 - 中国成年组运动员
```bash
# 搜索中国成年组运动员，支持模糊匹配
curl "http://localhost:8080/api/query/advanced?fuzzyKeyword=zhang&ageGroup=SENIOR&country=China&page=0&size=10"
```

## 技术实现

### 1. 模糊匹配检索实现
- **FuzzyQuery**: 支持拼写错误的模糊匹配
- **WildcardQuery**: 支持通配符匹配
- **PrefixQuery**: 支持前缀匹配
- **BoostQuery**: 支持相似度权重调整

### 2. 高级搜索实现
- **BooleanQuery**: 支持复杂的布尔逻辑组合
- **TermQuery**: 精确匹配查询
- **PhraseQuery**: 短语匹配查询
- **IntPoint/DoublePoint**: 数值范围查询

### 3. 智能搜索实现
- **多级权重**: 不同查询类型的权重设置
- **智能排序**: 基于相关性的结果排序
- **组合查询**: 精确匹配和模糊匹配的结合

### 4. 分页实现
- **Lucene层面分页**: 在索引层面进行分页查询
- **PageResponse**: 统一的分页响应格式
- **分页元数据**: 完整的分页信息

## 响应格式

所有检索接口都返回统一的分页响应格式：

```json
{
  "data": [
    {
      "id": "athlete_001",
      "name": "Zhang Wei",
      "age": "25",
      "location": "China",
      "kg": "73.0",
      "image": "photo_url"
    }
  ],
  "pageInfo": {
    "pageNo": 0,
    "pageSize": 10,
    "total": 100,
    "totalPages": 10,
    "hasPrevious": false,
    "hasNext": true
  }
}
```

## 注意事项

1. **参数验证**: 所有接口都包含完整的参数验证
2. **错误处理**: 统一的异常处理和错误响应
3. **性能优化**: 使用Lucene索引进行高效检索
4. **扩展性**: 支持动态添加新的检索条件
5. **兼容性**: 保持与现有接口的兼容性

## 总结

本系统实现了完整的柔道运动员信息检索功能，从基础的关键词检索到高级的模糊匹配和智能搜索，为用户提供了灵活、高效、准确的检索体验。通过多维度组合检索，用户可以精确地找到所需的运动员信息，满足不同场景的检索需求。 