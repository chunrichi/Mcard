# Mcard - 信息卡片应用

一个以卡片形式展示消息的 Android 应用，支持多信息源配置。

## 界面功能

### 消息列表页
- 展示消息卡片列表
- 点击卡片查看详情（弹框形式，支持 Markdown 渲染）
- 右上角刷新按钮，点击后显示加载动画
- 右上角设置按钮，跳转信息源配置页

### 信息源配置页
- 查看已配置的信息源列表
- 添加新信息源（支持名称、URL、认证配置）
- 启用/禁用信息源
- 删除信息源
- 清空所有数据

## 数据结构

### 消息 (Message)

```json
{
  "id": "string",
  "title": "string",
  "preview": "string",
  "content": "string",
  "timestamp": "number (Unix timestamp in milliseconds)",
  "source": "string"
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 消息唯一标识 |
| title | string | 是 | 消息标题 |
| preview | string | 是 | 预览文本（列表中显示） |
| content | string | 是 | 完整内容（弹框中显示，支持 Markdown） |
| timestamp | number | 是 | Unix 时间戳（毫秒） |
| source | string | 是 | 消息来源名称 |

**示例：**
```json
{
  "id": "msg_001",
  "title": "系统更新通知",
  "preview": "新版本 v2.0 已发布，包含多项功能优化...",
  "content": "## 更新内容\n\n- 性能提升 30%\n- 新增深色模式\n- 修复已知问题",
  "timestamp": 1713432000000,
  "source": "系统通知"
}
```

### 信息源 (Source)

```json
{
  "id": "string",
  "name": "string",
  "url": "string",
  "isEnabled": "boolean",
  "authType": "NONE | API_KEY | BEARER_TOKEN | BASIC_AUTH | CUSTOM_HEADER",
  "authKey": "string",
  "authValue": "string"
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 信息源唯一标识 |
| name | string | 是 | 信息源名称 |
| url | string | 否 | 数据接口 URL |
| isEnabled | boolean | 否 | 是否启用（默认 true） |
| authType | enum | 否 | 认证类型（默认 NONE） |
| authKey | string | 否 | 认证 Key（Header 名称或用户名） |
| authValue | string | 否 | 认证 Value（Token 或密码） |

**认证类型 (AuthType)：**
| 值 | 说明 | authKey | authValue |
|---|------|---------|-----------|
| NONE | 无认证 | - | - |
| API_KEY | API Key 认证 | Header 名称 | Key 值 |
| BEARER_TOKEN | Bearer Token | - | Token 值 |
| BASIC_AUTH | Basic 认证 | 用户名 | 密码 |
| CUSTOM_HEADER | 自定义 Header | Header 名称 | Header 值 |

**示例 - 无认证：**
```json
{
  "id": "source_001",
  "name": "科技新闻",
  "url": "https://api.example.com/feed",
  "isEnabled": true,
  "authType": "NONE"
}
```

**示例 - Bearer Token 认证：**
```json
{
  "id": "source_002",
  "name": "数据推送",
  "url": "https://api.example.com/webhook",
  "isEnabled": true,
  "authType": "BEARER_TOKEN",
  "authKey": "",
  "authValue": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**示例 - 自定义 Header 认证：**
```json
{
  "id": "source_003",
  "name": "内部 API",
  "url": "https://api.internal.com/v1/data",
  "isEnabled": true,
  "authType": "CUSTOM_HEADER",
  "authKey": "X-API-Key",
  "authValue": "your_secret_api_key"
}
```

## API 接口设计

### 获取消息列表
```
GET /api/messages
```

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceId | string | 否 | 按信息源筛选 |
| since | number | 否 | 返回指定时间之后的消息 |
| limit | number | 否 | 返回数量限制 |

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "id": "msg_001",
      "title": "系统更新通知",
      "preview": "新版本 v2.0 已发布...",
      "content": "## 更新内容\n\n- 性能提升",
      "timestamp": 1713432000000,
      "source": "系统通知"
    }
  ]
}
```

### 获取信息源列表
```
GET /api/sources
```

**响应示例：**
```json
{
  "success": true,
  "data": [
    {
      "id": "source_001",
      "name": "系统通知",
      "url": "https://api.example.com/notifications",
      "isEnabled": true,
      "authType": "BEARER_TOKEN",
      "authKey": "Authorization",
      "authValue": ""
    }
  ]
}
```

### 添加信息源
```
POST /api/sources
```

**请求体：**
```json
{
  "name": "新信息源",
  "url": "https://api.example.com/feed",
  "authType": "API_KEY",
  "authKey": "X-API-Key",
  "authValue": "your_key"
}
```

### 删除信息源
```
DELETE /api/sources/{id}
```

### 更新信息源
```
PUT /api/sources/{id}
```

## 技术栈

- **框架**: Jetpack Compose + Material 3
- **语言**: Kotlin
- **最低 SDK**: 34
- **目标 SDK**: 36
- **网络**: OkHttp 4.12.0
- **异步**: Kotlin Coroutines 1.7.3
- **架构**: 单 Activity + Compose Navigation

## 构建

```bash
./gradlew assembleDebug
```

APK 输出位置: `app/build/outputs/apk/debug/app-debug.apk`
