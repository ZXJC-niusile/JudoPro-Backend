# JudoPro 数据库设计文档

## 概述
本文档详细描述了 JudoPro 柔道管理系统的MySQL数据库表结构设计，主要用于用户管理功能。

## 重要说明
- **存储架构**: 系统采用混合存储架构
  - **Lucene索引**: 存储和检索运动员数据，提供高效的全文检索功能
  - **MySQL数据库**: 仅用于用户管理相关数据的结构化存储
- **登录方式**: 系统使用邮箱+密码的登录方式，已移除OAuth2第三方登录
- **用户管理**: 完善的用户注册、登录、信息管理功能

## 数据库表结构

### 1. 用户表 (users)

用户账户信息管理表

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 用户ID，主键自增 | 已定义 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名，唯一 | 已定义 |
| password | VARCHAR(255) | NOT NULL | - | 密码（加密存储） | 已定义 |
| email | VARCHAR(100) | UNIQUE | - | 邮箱地址，唯一 | 已定义 |
| phone | VARCHAR(20) | - | - | 手机号码 | 已定义 |
| avatar_url | VARCHAR(500) | - | - | 头像图片URL | 已定义 |
| real_name | VARCHAR(100) | - | - | 真实姓名 | 已定义 |
| gender | ENUM('MALE', 'FEMALE', 'OTHER') | - | - | 性别 | 已定义 |
| birth_date | DATE | - | - | 出生日期 | 已定义 |
| is_active | BOOLEAN | - | TRUE | 账户是否激活 | 已定义 |
| last_login | TIMESTAMP | - | - | 最后登录时间 | 已定义 |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_username` (username)
- `idx_email` (email)
- `idx_phone` (phone)

### 2. 用户文件表 (user_files) ⭐ 新增

用户上传文件信息管理表

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 文件ID，主键自增 | 已定义 |
| user_id | BIGINT | NOT NULL | - | 用户ID，外键关联users表 | 已定义 |
| original_filename | VARCHAR(255) | NOT NULL | - | 原始文件名 | 已定义 |
| stored_filename | VARCHAR(255) | NOT NULL | - | 存储文件名 | 已定义 |
| file_url | VARCHAR(500) | NOT NULL | - | 文件访问URL | 已定义 |
| file_type | ENUM('IMAGE', 'VIDEO', 'DOCUMENT', 'OTHER') | NOT NULL | - | 文件类型 | 已定义 |
| file_size | BIGINT | NOT NULL | - | 文件大小（字节） | 已定义 |
| mime_type | VARCHAR(100) | - | - | MIME类型 | 已定义 |
| description | TEXT | - | - | 文件描述 | 已定义 |
| download_count | INT | - | 0 | 下载次数 | 已定义 |
| upload_time | TIMESTAMP | - | CURRENT_TIMESTAMP | 上传时间 | 已定义 |
| last_access_time | TIMESTAMP | - | - | 最后访问时间 | 已定义 |
| is_deleted | BOOLEAN | - | FALSE | 是否已删除（软删除） | 已定义 |
| deleted_time | TIMESTAMP | - | - | 删除时间 | 已定义 |

**索引：**
- `idx_user_id` (user_id)
- `idx_file_type` (file_type)
- `idx_upload_time` (upload_time)
- `idx_is_deleted` (is_deleted)
- `idx_file_url` (file_url)

**外键约束：**
- `user_id` → `users(id)` ON DELETE CASCADE

### 3. 运动员表 (players) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中运动员数据存储在Lucene索引中，未在MySQL数据库中实现。

柔道运动员信息管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 运动员ID，主键自增 | 已定义 |
| name | VARCHAR(100) | NOT NULL | - | 运动员姓名 | 已定义 |
| age | INT | - | - | 年龄 | 已定义 |
| image | VARCHAR(500) | - | - | 头像图片URL | 已定义 |
| location | VARCHAR(200) | - | - | 所在地区 | 已定义 |
| location_icon | VARCHAR(500) | - | - | 地区图标URL | 已定义 |
| kg | DECIMAL(5,2) | - | - | 体重（公斤） | 已定义 |
| weight_class | VARCHAR(50) | - | - | 体重级别 | (未定义) |
| age_group | ENUM('YOUTH', 'JUNIOR', 'SENIOR', 'VETERAN') | - | - | 年龄组别 | (未定义) |
| country | VARCHAR(100) | - | - | 国家 | (未定义) |
| continent | ENUM('ASIA', 'EUROPE', 'AFRICA', 'NORTH_AMERICA', 'SOUTH_AMERICA', 'OCEANIA') | - | - | 大洲 | (未定义) |
| belt_rank | VARCHAR(50) | - | - | 段位等级 | (未定义) |
| club_team | VARCHAR(200) | - | - | 所属俱乐部/队伍 | (未定义) |
| coach | VARCHAR(100) | - | - | 教练姓名 | (未定义) |
| total_matches | INT | - | 0 | 总比赛场次 | (未定义) |
| wins | INT | - | 0 | 胜利场次 | (未定义) |
| losses | INT | - | 0 | 失败场次 | (未定义) |
| draws | INT | - | 0 | 平局场次 | (未定义) |
| ranking_points | INT | - | 0 | 排名积分 | (未定义) |
| world_ranking | INT | - | - | 世界排名 | (未定义) |
| is_active | BOOLEAN | - | TRUE | 是否现役 | (未定义) |
| bio | TEXT | - | - | 个人简介 | (未定义) |
| achievements | TEXT | - | - | 主要成就 | (未定义) |
| photos_json | JSON | - | - | 照片数据（JSON格式） | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_name` (name)
- `idx_country` (country)
- `idx_continent` (continent)
- `idx_weight_class` (weight_class)
- `idx_age_group` (age_group)
- `idx_world_ranking` (world_ranking)

### 4. 照片表 (photos) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中照片数据作为JSON存储在Lucene索引中，未在MySQL数据库中实现。

照片信息管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 照片ID，主键自增 | 已定义 |
| url | VARCHAR(500) | NOT NULL | - | 照片URL地址 | 已定义 |
| thumbnail_url | VARCHAR(500) | - | - | 缩略图URL | (未定义) |
| description | TEXT | - | - | 照片描述 | (未定义) |
| category | ENUM('MATCH', 'TRAINING', 'CEREMONY', 'PORTRAIT', 'OTHER') | - | 'OTHER' | 照片分类 | (未定义) |
| player_id | BIGINT | - | - | 关联运动员ID | (未定义) |
| event_name | VARCHAR(200) | - | - | 赛事名称 | (未定义) |
| event_date | DATE | - | - | 赛事日期 | (未定义) |
| location | VARCHAR(200) | - | - | 拍摄地点 | (未定义) |
| photographer | VARCHAR(100) | - | - | 摄影师 | (未定义) |
| tags | VARCHAR(500) | - | - | 标签（逗号分隔） | (未定义) |
| view_count | INT | - | 0 | 浏览次数 | (未定义) |
| like_count | INT | - | 0 | 点赞次数 | (未定义) |
| is_public | BOOLEAN | - | TRUE | 是否公开 | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_category` (category)
- `idx_player_id` (player_id)
- `idx_event_name` (event_name)
- `idx_event_date` (event_date)
- `idx_is_public` (is_public)

**外键约束：**
- `player_id` → `players(id)` ON DELETE SET NULL

### 5. 赛事表 (events) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中未实现赛事管理功能。

赛事信息管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 赛事ID，主键自增 | (未定义) |
| name | VARCHAR(200) | NOT NULL | - | 赛事名称 | (未定义) |
| description | TEXT | - | - | 赛事描述 | (未定义) |
| start_date | DATE | NOT NULL | - | 开始日期 | (未定义) |
| end_date | DATE | NOT NULL | - | 结束日期 | (未定义) |
| location | VARCHAR(200) | - | - | 举办地点 | (未定义) |
| country | VARCHAR(100) | - | - | 举办国家 | (未定义) |
| continent | ENUM('ASIA', 'EUROPE', 'AFRICA', 'NORTH_AMERICA', 'SOUTH_AMERICA', 'OCEANIA') | - | - | 举办大洲 | (未定义) |
| event_type | ENUM('WORLD_CHAMPIONSHIP', 'CONTINENTAL_CHAMPIONSHIP', 'NATIONAL_CHAMPIONSHIP', 'INTERNATIONAL_TOURNAMENT', 'LOCAL_TOURNAMENT') | - | - | 赛事类型 | (未定义) |
| age_groups | VARCHAR(200) | - | - | 参赛年龄组别（逗号分隔） | (未定义) |
| weight_classes | VARCHAR(500) | - | - | 参赛体重级别（逗号分隔） | (未定义) |
| max_participants | INT | - | - | 最大参赛人数 | (未定义) |
| registration_deadline | DATE | - | - | 报名截止日期 | (未定义) |
| entry_fee | DECIMAL(10,2) | - | - | 报名费用 | (未定义) |
| prize_money | DECIMAL(15,2) | - | - | 奖金总额 | (未定义) |
| status | ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED') | - | 'UPCOMING' | 赛事状态 | (未定义) |
| organizer | VARCHAR(200) | - | - | 主办方 | (未定义) |
| contact_email | VARCHAR(100) | - | - | 联系邮箱 | (未定义) |
| contact_phone | VARCHAR(50) | - | - | 联系电话 | (未定义) |
| website | VARCHAR(500) | - | - | 官方网站 | (未定义) |
| poster_url | VARCHAR(500) | - | - | 海报图片URL | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_name` (name)
- `idx_start_date` (start_date)
- `idx_end_date` (end_date)
- `idx_location` (location)
- `idx_country` (country)
- `idx_continent` (continent)
- `idx_event_type` (event_type)
- `idx_status` (status)

### 6. 参赛记录表 (participations) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中未实现参赛记录管理功能。

运动员参赛记录管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 参赛记录ID，主键自增 | (未定义) |
| player_id | BIGINT | NOT NULL | - | 运动员ID | (未定义) |
| event_id | BIGINT | NOT NULL | - | 赛事ID | (未定义) |
| weight_class | VARCHAR(50) | - | - | 参赛体重级别 | (未定义) |
| registration_date | TIMESTAMP | - | CURRENT_TIMESTAMP | 报名时间 | (未定义) |
| result | ENUM('GOLD', 'SILVER', 'BRONZE', 'PARTICIPATED', 'DISQUALIFIED', 'WITHDREW') | - | - | 比赛结果 | (未定义) |
| final_rank | INT | - | - | 最终排名 | (未定义) |
| points_earned | INT | - | 0 | 获得积分 | (未定义) |
| notes | TEXT | - | - | 备注信息 | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_player_id` (player_id)
- `idx_event_id` (event_id)
- `idx_weight_class` (weight_class)
- `idx_result` (result)
- `idx_final_rank` (final_rank)

**唯一约束：**
- `uk_player_event` (player_id, event_id)

**外键约束：**
- `player_id` → `players(id)` ON DELETE CASCADE
- `event_id` → `events(id)` ON DELETE CASCADE

### 7. 用户收藏表 (user_favorites) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中未实现用户收藏功能。

用户收藏功能管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 收藏记录ID，主键自增 | (未定义) |
| user_id | BIGINT | NOT NULL | - | 用户ID | (未定义) |
| target_type | ENUM('PLAYER', 'EVENT', 'PHOTO') | NOT NULL | - | 收藏目标类型 | (未定义) |
| target_id | BIGINT | NOT NULL | - | 收藏目标ID | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 收藏时间 | (未定义) |

**索引：**
- `idx_user_id` (user_id)
- `idx_target_type` (target_type)
- `idx_target_id` (target_id)

**唯一约束：**
- `uk_user_target` (user_id, target_type, target_id)

**外键约束：**
- `user_id` → `users(id)` ON DELETE CASCADE

### 7. 系统配置表 (system_config) - 仅设计文档

**重要说明**: 此表结构仅为设计文档，实际系统中未实现系统配置管理功能。

系统配置参数管理表（设计参考）

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 | 状态 |
|--------|----------|------|--------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 配置ID，主键自增 | (未定义) |
| config_key | VARCHAR(100) | NOT NULL, UNIQUE | - | 配置键 | (未定义) |
| config_value | TEXT | - | - | 配置值 | (未定义) |
| description | VARCHAR(500) | - | - | 配置描述 | (未定义) |
| config_type | ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') | - | 'STRING' | 配置类型 | (未定义) |
| is_editable | BOOLEAN | - | TRUE | 是否可编辑 | (未定义) |
| created_at | TIMESTAMP | - | CURRENT_TIMESTAMP | 创建时间 | (未定义) |
| updated_at | TIMESTAMP | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | (未定义) |

**索引：**
- `idx_config_key` (config_key)

## 数据库设计说明

### 字符集和排序规则
- 所有表使用 `utf8mb4` 字符集
- 排序规则为 `utf8mb4_unicode_ci`
- 存储引擎为 `InnoDB`

### 字段状态说明
- **已定义**：该字段在当前代码的实体类中已经定义
- **(未定义)**：该字段在数据库表中存在，但在代码实体类中尚未定义，需要后续开发时添加

### 开发建议
1. 对于标记为"(未定义)"的字段，建议根据业务需求逐步添加到对应的实体类中
2. 数据库表设计已考虑了完整的柔道管理系统功能，可支持渐进式开发
3. 所有时间戳字段都设置了自动更新机制
4. 外键约束确保了数据的完整性和一致性
5. 索引设计优化了常用查询的性能

### 表关系说明
- `players` ← `photos` (一对多)
- `players` ← `participations` → `events` (多对多)
- `users` ← `user_favorites` (一对多)
- 收藏表通过 `target_type` 和 `target_id` 实现多态关联

## 数据字典

### 枚举值说明

#### 性别 (gender)
- `MALE`: 男性
- `FEMALE`: 女性
- `OTHER`: 其他

#### 年龄组别 (age_group)
- `YOUTH`: 青少年组 (13-17岁)
- `JUNIOR`: 青年组 (18-20岁)
- `SENIOR`: 成年组 (21岁以上)
- `VETERAN`: 老将组 (35岁以上)

#### 大洲 (continent)
- `ASIA`: 亚洲
- `EUROPE`: 欧洲
- `AFRICA`: 非洲
- `NORTH_AMERICA`: 北美洲
- `SOUTH_AMERICA`: 南美洲
- `OCEANIA`: 大洋洲

#### 照片分类 (category)
- `MATCH`: 比赛照片
- `TRAINING`: 训练照片
- `CEREMONY`: 颁奖仪式照片
- `PORTRAIT`: 肖像照片
- `OTHER`: 其他类型

#### 赛事类型 (event_type)
- `WORLD_CHAMPIONSHIP`: 世界锦标赛
- `CONTINENTAL_CHAMPIONSHIP`: 洲际锦标赛
- `NATIONAL_CHAMPIONSHIP`: 全国锦标赛
- `INTERNATIONAL_TOURNAMENT`: 国际赛事
- `LOCAL_TOURNAMENT`: 地方赛事

#### 赛事状态 (status)
- `UPCOMING`: 即将举行
- `ONGOING`: 进行中
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

#### 比赛结果 (result)
- `GOLD`: 金牌
- `SILVER`: 银牌
- `BRONZE`: 铜牌
- `PARTICIPATED`: 参赛
- `DISQUALIFIED`: 取消资格
- `WITHDREW`: 退赛

#### 收藏目标类型 (target_type)
- `PLAYER`: 运动员
- `EVENT`: 赛事
- `PHOTO`: 照片

#### 配置类型 (config_type)
- `STRING`: 字符串类型
- `NUMBER`: 数字类型
- `BOOLEAN`: 布尔类型
- `JSON`: JSON对象类型

## 业务规则

### 数据完整性规则
1. **用户名唯一性**: 每个用户名在系统中必须唯一
2. **邮箱唯一性**: 每个邮箱地址只能注册一个账户
3. **运动员参赛唯一性**: 同一运动员不能在同一赛事中重复参赛
4. **用户收藏唯一性**: 用户不能重复收藏同一目标

### 数据验证规则
1. **密码安全**: 密码必须经过加密存储，建议使用BCrypt
2. **邮箱格式**: 邮箱地址必须符合标准格式
3. **体重范围**: 运动员体重应在合理范围内 (30-200kg)
4. **年龄合理性**: 运动员年龄应在合理范围内 (5-80岁)
5. **日期逻辑**: 赛事结束日期不能早于开始日期

### 级联删除规则
1. **删除运动员**: 相关参赛记录将被删除，照片关联将被置空
2. **删除赛事**: 相关参赛记录将被删除
3. **删除用户**: 相关收藏记录将被删除

## 性能优化建议

### 索引优化
1. **复合索引**: 考虑为常用查询组合创建复合索引
   - `players`: (country, weight_class, age_group)
   - `events`: (start_date, status, event_type)
   - `photos`: (player_id, category, is_public)

2. **分页查询优化**: 使用覆盖索引提高分页查询性能

3. **全文搜索**: 考虑为运动员姓名、赛事名称等字段添加全文索引

### 查询优化
1. **避免SELECT ***: 只查询需要的字段
2. **使用LIMIT**: 大数据量查询时使用分页
3. **索引覆盖**: 尽量使用覆盖索引避免回表查询

### 存储优化
1. **图片存储**: 建议使用CDN存储图片，数据库只存储URL
2. **JSON字段**: photos_json字段建议控制大小，避免存储过大数据
3. **归档策略**: 考虑对历史数据进行归档处理

## 安全考虑

### 数据安全
1. **密码加密**: 使用强加密算法存储用户密码
2. **敏感信息**: 手机号、邮箱等敏感信息考虑加密存储
3. **SQL注入防护**: 使用参数化查询防止SQL注入

### 访问控制
1. **权限管理**: 实现基于角色的访问控制(RBAC)
2. **数据隔离**: 确保用户只能访问授权的数据
3. **审计日志**: 记录重要操作的审计日志

## 扩展性考虑

### 水平扩展
1. **分库分表**: 大数据量时考虑按地区或时间分表
2. **读写分离**: 使用主从复制实现读写分离
3. **缓存策略**: 使用Redis缓存热点数据

### 功能扩展
1. **多语言支持**: 预留国际化字段
2. **移动端适配**: 考虑移动端特殊需求
3. **第三方集成**: 预留第三方系统集成接口

## 维护建议

### 定期维护
1. **索引维护**: 定期分析和优化索引
2. **数据清理**: 定期清理无效数据
3. **性能监控**: 监控慢查询和系统性能

### 备份策略
1. **全量备份**: 每日进行全量数据备份
2. **增量备份**: 每小时进行增量备份
3. **异地备份**: 重要数据进行异地备份

### 版本管理
1. **数据库版本**: 使用Flyway或Liquibase管理数据库版本
2. **变更记录**: 记录所有数据库结构变更
3. **回滚计划**: 制定数据库变更回滚计划

---

*文档生成时间：2024年*
*数据库版本：MySQL 8.0+*
*文档版本：v1.0*