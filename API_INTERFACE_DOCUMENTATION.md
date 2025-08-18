# 柔道运动员信息检索系统 - 完整API接口文档

## 概述

本系统提供完整的柔道运动员信息检索API，支持多种检索方式：基础检索、模糊匹配检索、高级搜索、智能搜索、组合检索等。所有接口都支持分页查询，返回统一的分页响应格式。

## 基础信息

- **Base URL**: `http://localhost:8080/query`
- **Content-Type**: `application/json;charset=UTF-8`
- **分页参数**: 所有接口都支持 `pageNo`(页码，从1开始) 和 `pageSize`(每页大小，默认10，最大100)
- **响应格式**: 统一使用 `QueryResponse<T>` 格式

## 通用响应格式

### 成功响应格式
```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    "data": [
      {
        "ID": "athlete_001",
        "NAME": "Zhang Wei",
        "AGE": "25",
        "LOCATION": "China",
        "KG": "73.0",
        "IMAGE": "photo_url",
        "LOCATION_ICON": "icon_url",
        "PHOTOS": "photos_url"
      }
    ],
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

### 错误响应格式
```json
{
  "success": false,
  "message": "错误信息",
  "data": null
}
```

## 1. 基础检索接口

### 1.1 关键词检索
**接口地址**: `GET /query/kw`

**功能说明**: 根据关键词在运动员姓名和国家中进行检索

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| kw | String | 是 | 检索关键词 | "zhang" |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/kw?kw=zhang&pageNo=1&pageSize=10
```

## 2. 多级检索接口

### 2.1 年龄组别检索
**接口地址**: `GET /query/ageGroup`

**功能说明**: 根据年龄组别检索运动员

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 可选值 |
|--------|------|------|------|--------|
| ageGroup | String | 是 | 年龄组别 | CADET(青少年15-17), JUNIOR(青年18-20), SENIOR(成年21-35), VETERAN(资深36+) |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/ageGroup?ageGroup=SENIOR&pageNo=1&pageSize=10
```

### 2.2 体重级别检索
**接口地址**: `GET /query/weightClass`

**功能说明**: 根据体重级别检索运动员

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 可选值 |
|--------|------|------|------|--------|
| weightClass | String | 是 | 体重级别代码 | -60, -66, -73, -81, -90, -100, +100 |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/weightClass?weightClass=-73&pageNo=1&pageSize=10
```

### 2.3 大洲检索
**接口地址**: `GET /query/continent`

**功能说明**: 根据大洲检索运动员（包含该大洲下的所有国家）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 可选值 |
|--------|------|------|------|--------|
| continent | String | 是 | 大洲 | ASIA, EUROPE, AFRICA, NORTH_AMERICA, SOUTH_AMERICA, OCEANIA |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/continent?continent=ASIA&pageNo=1&pageSize=10
```

### 2.4 国家检索
**接口地址**: `GET /query/country`

**功能说明**: 根据具体国家检索运动员

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| country | String | 是 | 国家名称 | "China" |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/country?country=China&pageNo=1&pageSize=10
```

## 3. 范围检索接口

### 3.1 年龄范围检索
**接口地址**: `GET /query/ageRange`

**功能说明**: 根据年龄范围检索运动员

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| minAge | Integer | 否 | 最小年龄，0-150 | 20 |
| maxAge | Integer | 否 | 最大年龄，0-150 | 30 |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/ageRange?minAge=20&maxAge=30&pageNo=1&pageSize=10
```

### 3.2 体重范围检索
**接口地址**: `GET /query/weightRange`

**功能说明**: 根据体重范围检索运动员

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| minWeight | Double | 否 | 最小体重，0-500公斤 | 60.0 |
| maxWeight | Double | 否 | 最大体重，0-500公斤 | 80.0 |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/weightRange?minWeight=60.0&maxWeight=80.0&pageNo=1&pageSize=10
```

## 4. 模糊匹配检索接口 ⭐ 新增

### 4.1 模糊匹配检索
**接口地址**: `GET /query/fuzzy`

**功能说明**: 支持模糊查询、通配符查询、前缀查询等多种模糊匹配方式

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| fuzzyKeyword | String | 是 | 模糊关键词 | "zhang" |
| similarity | Double | 否 | 相似度阈值，0.0-1.0 | 0.8 |
| page | Integer | 否 | 页码，从0开始，默认0 | 0 |
| size | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**功能特点**:
- 支持拼写错误的模糊匹配
- 支持通配符匹配(*和?)
- 支持前缀匹配
- 支持相似度阈值控制

**请求示例**:
```bash
GET /query/fuzzy?fuzzyKeyword=zhang&similarity=0.8&page=0&size=10
```

## 5. 高级搜索接口 ⭐ 新增

### 5.1 高级搜索
**接口地址**: `GET /query/advanced`

**功能说明**: 支持多字段组合检索，包括关键词、模糊关键词、年龄组别、年龄范围、体重级别、体重范围、大洲、国家等条件

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 否 | 精确关键词 | "zhang" |
| fuzzyKeyword | String | 否 | 模糊关键词 | "li" |
| similarity | Double | 否 | 相似度阈值，0.0-1.0 | 0.7 |
| ageGroup | String | 否 | 年龄组别 | "SENIOR" |
| minAge | Integer | 否 | 最小年龄，0-150 | 20 |
| maxAge | Integer | 否 | 最大年龄，0-150 | 30 |
| weightClass | String | 否 | 体重级别 | "LIGHTWEIGHT" |
| minWeight | Double | 否 | 最小体重，0-500公斤 | 60.0 |
| maxWeight | Double | 否 | 最大体重，0-500公斤 | 80.0 |
| continent | String | 否 | 大洲 | "ASIA" |
| country | String | 否 | 国家 | "China" |
| page | Integer | 否 | 页码，从0开始，默认0 | 0 |
| size | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**功能特点**:
- 支持多字段任意组合
- 支持布尔逻辑操作
- 支持参数验证
- 支持复杂查询条件

**请求示例**:
```bash
GET /query/advanced?keyword=zhang&fuzzyKeyword=li&similarity=0.7&ageGroup=SENIOR&minAge=20&maxAge=30&weightClass=LIGHTWEIGHT&minWeight=60.0&maxWeight=80.0&continent=ASIA&country=China&page=0&size=10
```

## 6. 智能搜索接口 ⭐ 新增

### 6.1 智能搜索
**接口地址**: `GET /query/smart`

**功能说明**: 结合精确匹配和模糊匹配的智能检索，优先返回精确匹配结果

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 是 | 搜索关键词 | "zhang" |
| page | Integer | 否 | 页码，从0开始，默认0 | 0 |
| size | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**功能特点**:
- 自动结合精确匹配和模糊匹配
- 智能权重排序
- 优先返回最相关结果

**请求示例**:
```bash
GET /query/smart?keyword=zhang&page=0&size=10
```

## 7. 组合检索接口

### 7.1 组合条件检索
**接口地址**: `GET /query/combined`

**功能说明**: 支持多个检索条件的组合查询

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 否 | 关键词 | "zhang" |
| ageGroup | String | 否 | 年龄组别 | "SENIOR" |
| minAge | Integer | 否 | 最小年龄 | 20 |
| maxAge | Integer | 否 | 最大年龄 | 30 |
| weightClass | String | 否 | 体重级别代码 | "-73" |
| minWeight | Double | 否 | 最小体重 | 60.0 |
| maxWeight | Double | 否 | 最大体重 | 80.0 |
| continent | String | 否 | 大洲 | "ASIA" |
| country | String | 否 | 国家 | "China" |
| pageNo | Integer | 否 | 页码，从1开始，默认1 | 1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 | 10 |

**请求示例**:
```bash
GET /query/combined?keyword=zhang&ageGroup=SENIOR&weightClass=-73&continent=ASIA&pageNo=1&pageSize=10
```

## 8. 数据获取接口

### 8.1 获取大洲列表
**接口地址**: `GET /query/continents`

**功能说明**: 获取所有可用的大洲列表

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "获取大洲列表成功",
  "data": [
    {
      "name": "ASIA",
      "chineseName": "亚洲",
      "englishName": "Asia"
    },
    {
      "name": "EUROPE",
      "chineseName": "欧洲",
      "englishName": "Europe"
    }
  ]
}
```

### 8.2 获取国家列表
**接口地址**: `GET /query/countries`

**功能说明**: 根据大洲获取国家列表

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| continent | String | 是 | 大洲 | "ASIA" |

**请求示例**:
```bash
GET /query/countries?continent=ASIA
```

### 8.3 获取Others国家列表
**接口地址**: `GET /query/others`

**功能说明**: 获取指定大洲的Others国家列表

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| continent | String | 是 | 大洲 | "ASIA" |

**请求示例**:
```bash
GET /query/others?continent=ASIA
```

### 8.4 动态添加Others国家
**接口地址**: `GET /query/addOthers`

**功能说明**: 动态添加国家到指定大洲的Others集合

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| country | String | 是 | 国家名称 | "NewCountry" |
| continent | String | 是 | 大洲 | "ASIA" |

**请求示例**:
```bash
GET /query/addOthers?country=NewCountry&continent=ASIA
```

### 8.5 获取年龄组别列表
**接口地址**: `GET /query/ageGroups`

**功能说明**: 获取所有可用的年龄组别列表

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "获取年龄组别列表成功",
  "data": [
    {
      "name": "CADET",
      "chineseName": "青少年",
      "minAge": 15,
      "maxAge": 17
    },
    {
      "name": "JUNIOR",
      "chineseName": "青年",
      "minAge": 18,
      "maxAge": 20
    }
  ]
}
```

### 8.6 获取体重级别列表
**接口地址**: `GET /query/weightClasses`

**功能说明**: 获取所有可用的体重级别列表

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "获取体重级别列表成功",
  "data": [
    {
      "name": "LIGHTWEIGHT",
      "chineseName": "轻量级",
      "code": "-73",
      "minWeight": 66,
      "maxWeight": 73
    },
    {
      "name": "MIDDLEWEIGHT",
      "chineseName": "中量级",
      "code": "-90",
      "minWeight": 81,
      "maxWeight": 90
    }
  ]
}
```

## 9. 错误处理

### 9.1 常见错误码
| 错误码 | 说明 | HTTP状态码 |
|--------|------|------------|
| INVALID_PARAMETER | 参数验证失败 | 400 |
| MISSING_PARAMETER | 缺少必需参数 | 400 |
| INVALID_AGE_RANGE | 年龄范围无效 | 400 |
| INVALID_WEIGHT_RANGE | 体重范围无效 | 400 |
| INVALID_SIMILARITY | 相似度阈值无效 | 400 |
| NO_CRITERIA | 未提供检索条件 | 400 |
| INTERNAL_ERROR | 服务器内部错误 | 500 |

## 10. 使用示例

### 10.1 基础检索示例
```bash
# 关键词检索
curl "http://localhost:8080/query/kw?kw=zhang&pageNo=1&pageSize=10"

# 年龄组别检索
curl "http://localhost:8080/query/ageGroup?ageGroup=SENIOR&pageNo=1&pageSize=10"

# 体重级别检索
curl "http://localhost:8080/query/weightClass?weightClass=-73&pageNo=1&pageSize=10"
```

### 10.2 模糊匹配检索示例
```bash
# 模糊匹配检索
curl "http://localhost:8080/query/fuzzy?fuzzyKeyword=zhang&similarity=0.8&page=0&size=10"
```

### 10.3 高级搜索示例
```bash
# 高级搜索 - 亚洲成年组轻量级运动员
curl "http://localhost:8080/query/advanced?ageGroup=SENIOR&weightClass=LIGHTWEIGHT&continent=ASIA&minAge=20&maxAge=30&minWeight=60.0&maxWeight=80.0&page=0&size=10"
```

### 10.4 智能搜索示例
```bash
# 智能搜索
curl "http://localhost:8080/query/smart?keyword=zhang&page=0&size=10"
```

### 10.5 组合检索示例
```bash
# 组合检索 - 中国成年组运动员
curl "http://localhost:8080/query/combined?ageGroup=SENIOR&country=China&pageNo=1&pageSize=10"
```

### 10.6 数据获取示例
```bash
# 获取大洲列表
curl "http://localhost:8080/query/continents"

# 获取年龄组别列表
curl "http://localhost:8080/query/ageGroups"

# 获取体重级别列表
curl "http://localhost:8080/query/weightClasses"

# 获取亚洲国家列表
curl "http://localhost:8080/query/countries?continent=ASIA"
```

## 11. 前端集成建议

### 11.1 检索界面设计
1. **搜索框**: 保留原有的关键词搜索功能
2. **筛选按钮**: 添加年龄组别、体重级别、大洲、国家等筛选按钮
3. **范围选择器**: 添加年龄范围和体重范围的选择器
4. **高级选项**: 添加模糊匹配和相似度控制选项
5. **组合检索**: 支持搜索框+筛选按钮的组合检索

### 11.2 数据获取
1. **初始化数据**: 页面加载时获取大洲、年龄组别、体重级别等基础数据
2. **动态加载**: 根据选择的大洲动态加载对应的国家列表
3. **Others处理**: 支持Others国家的显示和管理

### 11.3 用户体验
1. **实时搜索**: 支持输入时实时搜索
2. **结果高亮**: 在搜索结果中高亮匹配的关键词
3. **分页导航**: 提供完整的分页导航功能
4. **结果统计**: 显示检索结果的总数和当前页信息

## 12. 性能优化建议

### 12.1 接口优化
1. **缓存策略**: 对基础数据（大洲、年龄组别等）进行缓存
2. **分页优化**: 在Lucene层面进行分页查询
3. **索引优化**: 为常用查询字段建立合适的索引

### 12.2 前端优化
1. **防抖处理**: 对搜索输入进行防抖处理
2. **懒加载**: 对大量数据进行懒加载
3. **缓存策略**: 对检索结果进行本地缓存

## 总结

本API文档提供了完整的柔道运动员信息检索功能，包括：
- 基础检索功能（关键词、年龄组别、体重级别、地区等）
- 范围检索功能（年龄范围、体重范围）
- 模糊匹配检索功能（支持多种模糊匹配方式）
- 高级搜索功能（多字段组合检索）
- 智能搜索功能（智能权重排序）
- 数据获取功能（获取基础数据列表）
- Others功能（动态管理国家分类）

所有接口都支持分页查询，提供统一的响应格式和错误处理机制，为前端开发提供了完整的接口支持。 

### 用户管理接口

### 用户注册接口（邮箱注册）
- 路径：`/user/register`
- 方法：POST
- 参数：
  - username（string）：用户名
  - email（string）：邮箱
  - password（string）：密码（需为字母和数字组合，至少6位）
- 返回：注册成功的用户信息

### 用户登录接口（邮箱登录）
- 路径：`/user/login`
- 方法：POST
- 参数：
  - email（string）：邮箱
  - password（string）：密码
- 返回：登录成功的用户信息，失败返回401

### 获取当前用户信息
- 路径：`/user/me`
- 方法：GET
- 参数：无
- 返回：当前登录用户信息

## 13. 用户文件管理接口 ⭐ 新增

### 13.1 文件上传接口
**接口地址**: `POST /api/upload`

**功能说明**: 用户上传文件，支持图片、视频、文档等多种文件类型

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 上传的文件 |
| description | String | 否 | 文件描述 |

**请求示例**:
```bash
POST /api/upload
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="example.jpg"
Content-Type: image/jpeg

[文件内容]
--boundary
Content-Disposition: form-data; name="description"

这是一张示例图片
--boundary--
```

**响应示例**:
```json
{
  "success": true,
  "message": "文件上传成功",
  "data": {
    "id": 1,
    "originalFilename": "example.jpg",
    "fileUrl": "/uploads/2024/01/15/uuid_example.jpg",
    "fileType": "IMAGE",
    "fileSize": 1024000,
    "uploadTime": "2024-01-15T10:30:00"
  }
}
```

### 13.2 用户文件列表接口
**接口地址**: `GET /api/files`

**功能说明**: 获取当前用户的文件列表，支持分页、搜索和筛选

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 否 | 页码，从1开始，默认1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 |
| fileType | String | 否 | 文件类型筛选：IMAGE, VIDEO, DOCUMENT, OTHER |
| keyword | String | 否 | 文件名搜索关键词 |

**请求示例**:
```bash
GET /api/files?pageNo=1&pageSize=10&fileType=IMAGE&keyword=photo
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取文件列表成功",
  "data": {
    "data": [
      {
        "id": 1,
        "originalFilename": "photo1.jpg",
        "fileUrl": "/uploads/2024/01/15/uuid_photo1.jpg",
        "fileType": "IMAGE",
        "fileSize": 1024000,
        "description": "示例图片",
        "downloadCount": 5,
        "uploadTime": "2024-01-15T10:30:00",
        "lastAccessTime": "2024-01-15T15:20:00"
      }
    ],
    "pageInfo": {
      "pageNo": 1,
      "pageSize": 10,
      "total": 25,
      "totalPages": 3,
      "hasPrevious": false,
      "hasNext": true
    },
    "statistics": {
      "totalFiles": 25,
      "totalSize": 52428800,
      "imageCount": 15,
      "videoCount": 5,
      "documentCount": 3,
      "otherCount": 2
    }
  }
}
```

### 13.3 文件下载接口
**接口地址**: `GET /api/files/{fileId}/download`

**功能说明**: 下载指定文件，用户只能下载自己的文件

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | Long | 是 | 文件ID（路径参数） |

**请求示例**:
```bash
GET /api/files/1/download
```

**响应**: 直接返回文件流，设置适当的Content-Type和Content-Disposition头

### 13.4 文件删除接口
**接口地址**: `DELETE /api/files/{fileId}`

**功能说明**: 删除指定文件，用户只能删除自己的文件（软删除）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | Long | 是 | 文件ID（路径参数） |

**请求示例**:
```bash
DELETE /api/files/1
```

**响应示例**:
```json
{
  "success": true,
  "message": "文件删除成功",
  "data": null
}
```

### 13.5 文件统计接口
**接口地址**: `GET /api/files/statistics`

**功能说明**: 获取当前用户的文件统计信息

**请求参数**: 无

**请求示例**:
```bash
GET /api/files/statistics
```

**响应示例**:
```json
{
  "success": true,
  "message": "获取统计信息成功",
  "data": {
    "totalFiles": 25,
    "totalSize": 52428800,
    "totalSizeFormatted": "50.0 MB",
    "imageCount": 15,
    "videoCount": 5,
    "documentCount": 3,
    "otherCount": 2,
    "totalDownloads": 150,
    "recentUploads": 3
  }
}
```

### 13.6 错误响应

**文件不存在**:
```json
{
  "success": false,
  "message": "文件不存在或已被删除",
  "data": null
}
```

**权限不足**:
```json
{
  "success": false,
  "message": "您没有权限访问此文件",
  "data": null
}
```

**文件上传失败**:
```json
{
  "success": false,
  "message": "文件上传失败：文件大小超过限制",
  "data": null
}
```

### 13.7 注意事项

1. **用户隔离**: 所有文件操作都基于当前登录用户，确保用户只能操作自己的文件
2. **文件类型**: 支持常见的图片、视频、文档格式，具体支持的格式由后端配置决定
3. **文件大小**: 单个文件大小限制为50MB
4. **软删除**: 文件删除采用软删除机制，不会立即从磁盘删除
5. **下载统计**: 每次文件下载都会更新下载次数和最后访问时间
6. **分页查询**: 文件列表支持分页查询，避免一次性加载大量数据