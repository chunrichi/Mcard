# Mcard Server - 消息源服务器

为 Mcard Android 应用提供消息数据的简易后端服务。

## 功能

- 提供消息接口供 Android 客户端拉取
- 支持多信息源配置
- 支持增量同步（通过 `since` 参数）
- 支持多种认证方式

## 安装

```bash
pip install -r requirements.txt
```

## 运行

```bash
python main.py
```

服务将在 `http://0.0.0.0:8000` 启动。

## 接口

### 获取消息列表

```
GET /api/messages
```

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| since | number | 否 | 返回指定时间之后的消息（毫秒时间戳） |
| limit | number | 否 | 返回数量限制 |

**响应示例：**
```json
[
  {
    "id": "msg_001",
    "title": "系统更新通知",
    "preview": "新版本 v2.0 已发布...",
    "content": "## 更新内容\n\n- 性能提升 30%",
    "timestamp": 1713432000000,
    "source": "系统通知"
  }
]
```

### 健康检查

```
GET /health
```

**响应示例：**
```json
{
  "status": "ok"
}
```

## 消息格式

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
| preview | string | 是 | 预览文本 |
| content | string | 是 | 完整内容（支持 Markdown） |
| timestamp | number | 是 | Unix 时间戳（毫秒） |
| source | string | 是 | 消息来源名称 |
