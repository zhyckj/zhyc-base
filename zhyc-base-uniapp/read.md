# uni-app 移动端端口说明

## 端口与用途

- 建议 H5 本地端口：`5174`
- 应用名称：`zhyc-base-uniapp`
- 模块路径：`zhyc-base-uniapp`

移动端面向后续 H5、小程序和 App 多端生成，首期覆盖移动工作台、流程待办、消息、采购申请、采购订单和个人中心。

## 技术说明

- uni-app
- Vue 3
- Vite 7
- TypeScript
- `@dcloudio/vite-plugin-uni`

移动端请求统一走 `/api`，首期用于验证移动审批、消息、工作台和采购样板业务链路。由于后台管理端默认占用 `5173`，本地同时启动时建议移动端 H5 使用 `5174`。

## 启动前置

- Node.js 与 npm 可用。
- 核心平台 `8081` 已启动。
- 认证中心 `8090` 已启动。
- 移动端 Token、租户和用户上下文需与核心平台 Shiro 权限及租户隔离保持一致。

## 启动方式

进入 uni-app 目录：

```bash
rtk npm run dev:h5 -- --port 5174
```

访问地址：

```text
http://127.0.0.1:5174
```

移动端登录页：

```text
http://127.0.0.1:5174/pages/auth/login
```

H5 本地开发已通过 `vite.config.ts` 代理：

- `/api` -> `http://127.0.0.1:8081`
- `/auth-center` -> `http://127.0.0.1:8090`

小程序、App、支付宝小程序等目标端不能依赖 H5 代理，需通过环境变量配置完整 HTTPS 地址：

```bash
VITE_MOBILE_PLATFORM_API_BASE_URL=https://platform.example.com/api
VITE_MOBILE_AUTH_API_BASE_URL=https://auth.example.com
```

## 验证方式

```bash
rtk npm run test:mobile-auth
rtk npm run verify:shell
```

H5 构建：

```bash
rtk npm run build:h5
```
