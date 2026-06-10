# 智能排课系统前端

这是 `openclaw-bs` 项目的 Vue 3 前端，负责管理员、教师、学生三端页面展示，以及和 Spring Boot 后端进行接口交互。

## 1. 技术栈

- Vue 3
- Vite 7
- Pinia
- Vue Router 4
- Element Plus
- Axios

## 2. 本地开发

安装依赖：

```bash
npm install
```

启动开发环境：

```bash
npm run dev
```

默认访问地址通常为：

- `http://localhost:5173`

## 3. 登录入口

- 学生/教师统一登录页：`/login`
- 管理员登录页：`/admin-login`

说明：

- 学生和教师共用同一个登录页，通过页签切换角色
- 管理员单独使用后台登录页

## 4. 接口请求方式

前端已经统一通过 `/api` 访问后端：

- 开发环境：由 Vite 代理到 `http://localhost:8084`
- Docker 环境：由 Nginx 代理到后端容器 `backend:8084`

这意味着页面代码本身不再依赖写死的 `localhost:8084`。

## 5. 打包

执行：

```bash
npm run build
```

打包结果会输出到：

- `dist/`

## 6. Docker 部署配合

当前项目已经支持 Docker Compose 部署。

如果使用项目根目录的编排文件启动：

```bash
docker compose up -d --build
```

前端默认访问地址为：

- `http://localhost:8080`

## 7. 目录说明

主要目录：

- `src/views/`：页面
- `src/components/`：通用组件
- `src/api/`：接口封装
- `src/router/`：路由配置
- `src/store/`：状态管理
- `src/utils/`：请求和工具函数

## 8. 当前状态

前端已经完成以下工程化改造：

- 兼容 JWT 鉴权
- 统一 Bearer Token 注入
- 统一 API 地址生成
- 兼容本地开发和 Docker 部署
