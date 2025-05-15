# ? 查询接口文档

## 1. 接口基本信息

- **接口名称**：关键词检索接口
- **接口地址**：`/query/kw`
- **请求方式**：`GET`
- **请求格式**：`application/x-www-form-urlencoded`
- **响应格式**：`application/json`
- **接口描述**：根据关键词、年龄、体重、地点筛选，支持分页查询。

---



## 2. 请求参数

| 参数名   | 类型   | 必填 | 默认值 | 描述              |
|----------|--------|------|--------|-------------------|
| kw       | String | 否   | 无     | 搜索关键词（模糊匹配） |
| age      | String | 否   | 无     | 按年龄筛选         |
| kg       | String | 否   | 无     | 按体重筛选         |
| location | String | 否   | 无     | 按地区筛选         |
| pageNo   | int    | 否   | 1      | 页码，从1开始       |
| pageSize | int    | 否   | 10     | 每页返回记录数     |

---

## 3. 返回参数

| 参数名 | 类型         | 描述                   |
|--------|--------------|------------------------|
| code   | int          | 状态码（0成功，1失败）  |
| msg    | String       | 返回消息                 |
| data   | List\<Object\> | 返回的数据列表（分页）    |

### data字段结构说明

| 字段名         | 类型   | 描述           |
|----------------|--------|----------------|
| ID             | String | 记录ID         |
| NAME           | String | 姓名           |
| AGE            | String | 年龄           |
| IMAGE          | String | 头像图片URL     |
| LOCATION       | String | 地区           |
| LOCATION_ICON  | String | 地区图标URL     |
| KG             | String | 体重           |

---

## 4. 请求示例

### 请求URL示例

