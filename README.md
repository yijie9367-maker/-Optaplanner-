# AI智能排课系统 (AI Smart Course Scheduling System)

> 基于 AI 的大学智能排课辅助系统，包含 **PC管理后台**、**微信小程序**、**Spring Boot 后端** 三端完整代码。

![Tech Stack](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green?logo=springboot)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs)
![Vite](https://img.shields.io/badge/Vite-7-646CFF?logo=vite)
![WeChat](https://img.shields.io/badge/WeChat-MiniProgram-07C160?logo=wechat)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![OptaPlanner](https://img.shields.io/badge/OptaPlanner-ConstraintSolving-FF6F00)

---

## 📖 项目简介

本项目是一个完整的大学智能排课系统，利用 **OptaPlanner 约束求解引擎** 实现自动排课，集成 **阿里云百炼 AI大模型** 提供智能助手服务。系统支持三种角色：

| 角色 | 访问端 | 主要功能 |
|------|--------|----------|
| 🎓 学生 | 微信小程序 / PC Web | 课表查看、考试查询、论坛交流、AI智能助手 |
| 👨‍🏫 教师 | PC Web | 课程管理、考试安排、成绩录入、学生沟通 |
| 🔧 管理员 | PC Web | 数据管理、排课引擎、论坛审核、敏感词过滤、系统配置 |

---

## 🏗️ 项目结构

```
aischedule/
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/com/wj/aischedule/
│   │   ├── controller/      # REST API 控制器（含 Chatbot AI 接口）
│   │   ├── service/         # 业务逻辑层
│   │   ├── entity/          # JPA 实体类
│   │   ├── repository/      # 数据访问层
│   │   ├── security/        # JWT 认证与权限控制
│   │   ├── config/          # 配置类（CORS、Security、Database）
│   │   ├── dto/             # 数据传输对象
│   │   └── optaplanner/     # OptaPlanner 排课约束求解
│   ├── src/main/resources/  # 配置与静态资源
│   │   ├── application.yml  # 应用配置
│   │   ├── solverConfig.xml # OptaPlanner 求解器配置
│   │   └── static/          # chatbot.html (百炼 SDK 页面)
│   ├── pom.xml              # Maven 依赖
│   └── Dockerfile           # Docker 镜像构建
│
├── miniprogram/             # 微信小程序前端
│   ├── pages/
│   │   ├── schedule/        # 📅 课表（周视图/列表视图）
│   │   ├── exam/            # 📝 考试安排
│   │   ├── forum/           # 💬 论坛广场
│   │   ├── forum-detail/    # 帖子详情与评论
│   │   ├── post-editor/     # 发帖编辑器
│   │   ├── mine/            # 👤 个人中心
│   │   └── ai-assistant/    # 🤖 AI 智能助手（web-view）
│   ├── components/          # 通用组件
│   ├── utils/               # 工具函数 & API 封装
│   ├── app.js / app.json / app.wxss
│   └── project.config.json
│
├── pc-frontend/             # Vue 3 PC 管理端
│   ├── src/
│   │   ├── views/
│   │   │   ├── admin/       # 管理员页面（Dashboard、排课、论坛审核等）
│   │   │   ├── teacher/     # 教师页面（课程、考试、成绩）
│   │   │   ├── student/     # 学生页面
│   │   │   ├── forum/       # 论坛页面
│   │   │   └── login/       # 登录页
│   │   ├── api/             # 接口封装
│   │   ├── router/          # 路由配置
│   │   ├── store/           # Pinia 状态管理
│   │   ├── components/      # 通用组件
│   │   ├── composables/     # 组合式函数
│   │   └── utils/           # 请求 & 工具函数
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
├── docs/                    # 📖 项目文档
│   ├── 需求文档-v2.md       # 完整需求规格说明
│   ├── 开发总结与心得.md     # AI 辅助开发实践经验
│   ├── 测试报告.md           # 测试用例与结果
│   ├── 测试与运维手册.md      # 部署运维指南
│   ├── AI智能体-小程序操作指引手册.md
│   └── ...                  # 数据库设计、子系统说明等
│
├── docker/                  # 🐳 Docker 部署
│   ├── docker-compose.yml   # 一键部署编排
│   └── *.md                 # 部署说明文档
│
├── .gitignore
└── README.md
```

---

## 🚀 技术栈

### 后端
| 技术 | 说明 |
|------|------|
| Java 17 | 运行环境 |
| Spring Boot 2.7.18 | 应用框架 |
| Spring Data JPA | ORM 持久化 |
| MySQL 8.0 | 关系型数据库 |
| Spring Security + JWT | 认证鉴权 |
| OptaPlanner 8.x | 智能排课约束求解引擎 |
| 阿里云百炼 DashScope | AI 智能助手大模型 |
| Maven | 项目构建 |

### 微信小程序
| 技术 | 说明 |
|------|------|
| 微信原生框架 | WXML + WXSS + JS |
| web-view | 嵌入阿里云百炼 AppFlow Chat SDK |

### PC 管理端
| 技术 | 说明 |
|------|------|
| Vue 3 (Composition API) | 前端框架 |
| Vite 7 | 构建工具 |
| Element Plus 2.x | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由管理 |
| Axios | HTTP 请求 |

---

## ⚡ 快速开始

### 前置要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Node.js 18+** (PC前端)
- **微信开发者工具** (小程序)

### 1️⃣ 后端启动

```bash
# 进入后端目录
cd backend

# 配置数据库（修改 src/main/resources/application.yml）
# 或通过环境变量设置：
#   DB_URL=jdbc:mysql://localhost:3306/aischedule
#   DB_USERNAME=root
#   DB_PASSWORD=your_password

# 启动后端（默认端口 8084）
./mvnw spring-boot:run      # Linux/Mac
mvnw.cmd spring-boot:run    # Windows
```

### 2️⃣ PC 前端启动

```bash
cd pc-frontend

# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:5173）
npm run dev

# 生产构建
npm run build
```

### 3️⃣ 微信小程序启动

1. 打开 **微信开发者工具**
2. 导入项目目录 `miniprogram/`
3. 填写 AppID（或使用测试号）
4. 修改 `utils/api.js` 中的 `baseUrl` 为你的后端地址
5. 编译预览

### 4️⃣ Docker 一键部署

```bash
# 使用 Docker Compose 同时启动前后端
cd docker
docker compose up -d --build

# 访问地址：
#   PC前端: http://localhost:8080
#   后端API: http://localhost:8084
```

---

## 🔐 登录说明

| 角色 | PC端登录入口 | 说明 |
|------|-------------|------|
| 学生 | `/login` (学生页签) | 姓名 + 密码 |
| 教师 | `/login` (教师页签) | 姓名 + 密码 |
| 管理员 | `/admin-login` | 管理员账号 |

小程序端学生通过 **我的** 页面登录。

---

## 📡 核心 API 概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/student/login` | 学生登录 |
| | POST | `/teacher/login` | 教师登录 |
| | POST | `/admin/login` | 管理员登录 |
| 课表 | GET | `/schedule/listByClassGroupId` | 获取班级课表 |
| 考试 | GET | `/exam/listByClassGroupId` | 获取考试安排 |
| 论坛 | GET/POST | `/forum/posts` | 帖子列表/发帖 |
| | POST | `/forum/posts/{id}/vote` | 点赞 |
| | POST | `/forum/posts/{id}/comments` | 评论 |
| AI助手 | POST | `/chatbot/chat` | AI 对话 |
| | GET | `/chatbot/anonymous-config` | 获取匿名配置 |
| 排课 | POST | `/scheduling/solve` | 启动排课求解 |

---

## 🧠 AI 特色功能

### 智能排课引擎
基于 **OptaPlanner** 约束求解框架，支持：
- 教师冲突检测（同一时间不能上两门课）
- 教室容量约束
- 班级不冲突约束
- 自定义排课偏好配置

### AI 智能助手
集成 **阿里云百炼 DashScope**，学生可以：
- 自然语言查询课表、考试信息
- 课程内容问答
- 校园生活咨询
- 通过小程序 web-view 直接与 AI 对话

---

## 📝 开发心得

本项目是一次完整的 **"人 + AI 协作"** 软件工程实践。从需求分析、原型设计、编码开发到测试运维，深度借助了 AI 工具链：

| 阶段 | 工具 | 作用 |
|------|------|------|
| 需求分析 | DeepSeek | 将模糊想法结构化 |
| 原型设计 | Stitch + DeepSeek | 设计稿直接转代码 |
| 编码开发 | DeepSeek | 全流程结对编程 |
| 测试 | DeepSeek | 自动生成47条测试用例 |
| 文档 | DeepSeek | 结构化文档生成 |
| AI 功能 | 阿里云百炼 | 产品内置 AI 助手 |

> 详细开发心得请参阅 [docs/开发总结与心得.md](docs/开发总结与心得.md)

---

## 📄 许可证

本项目仅用于学术研究与学习目的。

---

*Made by 王杰 (软件工程2201) — 2026*
