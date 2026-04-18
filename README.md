# Mcard - 信息卡片应用

一个以卡片形式展示消息的 Android 应用，支持多信息源配置和本地缓存。

## 功能特性

### 消息管理
- 消息列表展示，支持本地缓存
- 点击卡片全屏查看详情，支持 Markdown 渲染
- 左右滑动切换上一条/下一条消息
- 下拉刷新，自动同步最新消息

### 信息源配置
- 支持添加多个信息源
- 多种认证方式：无、API Key、Bearer Token、Basic Auth、自定义 Header
- 启用/禁用信息源
- 可调整同步时间戳，实现增量同步
- 清空所有数据

### 技术特点
- Jetpack Compose + Material 3
- Kotlin Coroutines 异步处理
- OkHttp 网络请求
- SharedPreferences 本地存储

## 界面预览

### 消息列表页
- 卡片形式展示消息
- 显示标题、预览和时间
- 点击查看详情，全屏展示支持滑动切换

### 信息源配置页
- 管理所有信息源
- 配置认证信息
- 调整同步时间

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

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 消息唯一标识 |
| title | string | 消息标题 |
| preview | string | 预览文本（列表中显示） |
| content | string | 完整内容（支持 Markdown） |
| timestamp | number | Unix 时间戳（毫秒） |
| source | string | 消息来源名称 |

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

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 信息源唯一标识 |
| name | string | 信息源名称 |
| url | string | 数据接口 URL |
| isEnabled | boolean | 是否启用 |
| authType | enum | 认证类型 |
| authKey | string | Header 名称或用户名 |
| authValue | string | Token 或密码 |

**认证类型 (AuthType)：**
| 值 | 说明 |
|---|------|
| NONE | 无认证 |
| API_KEY | API Key 认证 |
| BEARER_TOKEN | Bearer Token |
| BASIC_AUTH | Basic 认证 |
| CUSTOM_HEADER | 自定义 Header |

## API 接口

应用会从配置的信息源 URL 获取消息列表，支持 `since` 参数实现增量同步。

### 消息列表响应格式

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

## 构建

```bash
./gradlew assembleDebug
```

APK 输出位置: `app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- **框架**: Jetpack Compose + Material 3
- **语言**: Kotlin
- **最低 SDK**: 34
- **目标 SDK**: 36
- **网络**: OkHttp 4.12.0
- **异步**: Kotlin Coroutines 1.7.3
- **架构**: 单 Activity + Compose Navigation
