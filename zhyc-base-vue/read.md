# 后台管理端口说明

## 端口与用途

- 默认端口：`5173`
- 应用名称：`zhyc-base-vue`
- 模块路径：`zhyc-base-vue`

后台管理端提供系统管理、SaaS 租户、低代码中心、工作流中心、开放平台管理、采购样板和扩展模块的管理界面。

## 技术说明

- Vue 3
- Vite 7
- TypeScript
- Ant Design Vue
- Pinia
- Vue Router
- Vben Admin 风格后台架构

本地开发时，Vite 将 `/api` 代理到核心平台 `http://127.0.0.1:8081`。统一登录跳转到认证中心 `http://127.0.0.1:8090/oauth2/authorize`，回调地址为 `http://127.0.0.1:5173/auth/callback`。

## 启动前置

- Node.js 与 npm 可用。
- 核心平台 `8081` 已启动。
- 认证中心 `8090` 已启动。
- `VITE_AUTH_AUTHORIZATION_ENDPOINT`、`VITE_AUTH_CLIENT_ID`、`VITE_AUTH_REDIRECT_URI`、`VITE_AUTH_SCOPE` 已配置。

## 启动方式

进入后台管理端目录：

```bash
rtk npm run dev
```

访问地址：

```text
http://127.0.0.1:5173
```

## 验证方式

```bash
rtk npm run verify:shell
```

生产构建：

```bash
rtk npm run build
```
