from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import uuid

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 本服务器的消息存储
messages = [
    {
        "id": "msg_001",
        "title": "系统更新通知",
        "preview": "新版本 v2.0 已发布，包含多项功能优化...",
        "content": "## 更新内容\n\n- 性能提升 30%\n- 新增深色模式\n- 修复已知问题\n\n## 升级建议\n\n建议所有用户更新到最新版本。",
        "timestamp": 1713432000000,
        "source": "本服务器",
        "url": "https://github.com/chunrichi/Mcard/releases/tag/v1.0.0"
    },
    {
        "id": "msg_002",
        "title": "周末活动提醒",
        "preview": "本周六下午 3 点有技术分享会...",
        "content": "### 活动详情\n\n- **时间**: 周六 15:00\n- **地点**: 会议室 A\n- **主题**: Jetpack Compose 最佳实践\n\n欢迎参加！",
        "timestamp": 1713345600000,
        "source": "本服务器",
        "url": "https://example.com/event/123"
    },
    {
        "id": "msg_003",
        "title": "安全警告",
        "preview": "检测到异常登录尝试...",
        "content": "**警告**: 您的账号在非常用地点登录。\n\n- IP: 192.168.1.100\n- 位置: 北京市\n- 时间: 2024-04-18 10:30\n\n如果不是您本人操作，请立即修改密码。",
        "timestamp": 1713259200000,
        "source": "本服务器"
    },
    {
        "id": "msg_004",
        "title": "Markdown 链接示例",
        "preview": "点击查看如何创建链接...",
        "content": "## 链接示例\n\n这是一个 [百度](https://www.baidu.com) 链接。\n\n这是一个 [GitHub](https://github.com) 链接。\n\n无链接文本。",
        "timestamp": 1713172800000,
        "source": "本服务器",
        "url": "https://example.com/markdown-links"
    }
]


# 获取本服务器的所有消息
@app.get("/api/messages")
async def get_messages(since: Optional[int] = None, limit: Optional[int] = None):
    filtered = messages
    if since:
        filtered = [m for m in filtered if m["timestamp"] > since]
    if limit:
        filtered = filtered[:limit]
    return filtered


# 添加消息（用于测试）
@app.post("/api/messages")
async def add_message(title: str, preview: str, content: str, url: Optional[str] = None):
    new_message = {
        "id": f"msg_{uuid.uuid4().hex[:8]}",
        "title": title,
        "preview": preview,
        "content": content,
        "timestamp": int(uuid.uuid1().time * 1000),
        "source": "本服务器"
    }
    if url:
        new_message["url"] = url
    messages.append(new_message)
    return {"success": True, "data": new_message}


# 健康检查
@app.get("/health")
async def health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
