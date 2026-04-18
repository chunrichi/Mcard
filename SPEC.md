# Mcard - 信息卡片应用规范

## 1. 项目概述

- **项目名称**: Mcard
- **项目类型**: Android 原生应用 (Jetpack Compose)
- **核心功能**: 以卡片形式展示消息列表，支持信息源配置管理

## 2. 技术栈

- **框架**: Jetpack Compose + Material 3
- **语言**: Kotlin
- **最低 SDK**: 35
- **目标 SDK**: 36
- **架构**: 单 Activity + Compose Navigation

## 3. UI/UX 规范

### 3.1 视觉风格

- **配色方案**: 纯黑白配色
  - 背景色: `#FFFFFF` (纯白)
  - 卡片背景: `#FFFFFF` (纯白)
  - 主文字: `#000000` (纯黑)
  - 次要文字: `#666666` (深灰)
  - 分割线/边框: `#E0E0E0` (浅灰)
  - 悬浮阴影: `#000000` 15% 透明度

- **圆角**: 直角 (0dp radius) - 无圆角

- **悬浮效果**: 轻微阴影
  - 阴影高度: 2dp
  - 阴影透明度: 15%
  - 偏移: (0, 1dp)

### 3.2 屏幕结构

两个主要界面：

**界面 1: 消息卡片列表 (MessageListScreen)**
- 顶部标题栏: "消息" 居中
- 右侧菜单图标: 跳转至配置页面
- 内容区域: 垂直滚动的卡片列表
- 每张卡片可点击展开/收起

**界面 2: 信息源配置 (SourceConfigScreen)**
- 顶部标题栏: "信息源配置" + 返回按钮
- 内容区域: 信息源列表
- 支持添加/删除信息源

### 3.3 卡片组件设计

**消息卡片 (MessageCard)**
```
┌─────────────────────────────┐
│ 标题                    时间  │
│ ──────────────────────────── │
│ 预览文字...                  │
│                    [展开图标] │
└─────────────────────────────┘

展开状态:
┌─────────────────────────────┐
│ 标题                    时间  │
│ ──────────────────────────── │
│ 完整内容文字...              │
│                              │
│ 额外信息1 | 额外信息2        │
└─────────────────────────────┘
```

- 卡片内边距: 16dp
- 标题字号: 16sp, Medium weight
- 预览文字: 14sp, Regular weight
- 时间: 12sp, Regular weight, 右对齐
- 分割线: 1dp, #E0E0E0
- 卡片间距: 12dp

### 3.4 组件状态

**卡片状态**
- 默认: 白色背景，轻微阴影
- 按下: 阴影加深，背景色 #F5F5F5
- 展开: 显示完整内容，展开图标旋转 180°

## 4. 功能规范

### 4.1 消息卡片列表

- 显示消息卡片列表，支持垂直滚动
- 点击卡片: 展开/收起详情
- 数据来源: 本地 Mock 数据 (后续可扩展为网络请求)
- 空状态: 显示 "暂无消息" 提示

### 4.2 信息源配置

- 显示已配置的信息源列表
- 每个信息源显示: 名称、类型、状态
- 支持添加新信息源 (弹出对话框输入)
- 支持删除信息源 (滑动删除或点击删除图标)
- 支持启用/禁用信息源

### 4.3 信息源类型 (Mock)

- RSS 订阅
- 邮件通知
- 社交媒体
- 自定义 Webhook

## 5. 数据模型

### Message (消息)
```
- id: String
- title: String
- preview: String
- content: String
- timestamp: Long
- source: String
- isExpanded: Boolean
```

### Source (信息源)
```
- id: String
- name: String
- type: SourceType (RSS, EMAIL, SOCIAL, WEBHOOK)
- isEnabled: Boolean
```

## 6. 文件结构

```
app/src/main/java/com/example/mcard/
├── MainActivity.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt          # 黑白配色定义
│   │   ├── Theme.kt          # 主题配置
│   │   └── Type.kt           # 字体样式
│   ├── components/
│   │   └── MessageCard.kt    # 可展开消息卡片组件
│   ├── screens/
│   │   ├── MessageListScreen.kt   # 消息列表页
│   │   └── SourceConfigScreen.kt  # 信息源配置页
│   ├── navigation/
│   │   └── AppNavigation.kt       # 导航配置
│   └── data/
│       ├── model/
│       │   ├── Message.kt         # 消息数据模型
│       │   └── Source.kt          # 信息源数据模型
│       └── MockData.kt            # Mock 数据
```
