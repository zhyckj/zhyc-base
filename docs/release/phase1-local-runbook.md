# ZHYC 快速开发平台首期本地运行手册

本文档用于首期本地联调和交付验收，覆盖核心平台、认证中心、开放 API 网关、后台管理端和 uni-app 移动端。

在线演示环境可用于先体验框架功能和页面效果：

| 项目 | 内容 |
| --- | --- |
| 演示地址 | [https://base.zhyc-cloud.com/](https://base.zhyc-cloud.com/) |
| 演示账号 | `admin` |
| 演示密码 | `zhyc@123456` |

演示环境仅用于功能体验和产品交流，数据可能定期重置。请勿录入真实业务数据、生产密钥、个人隐私或其他敏感信息；本地、测试和正式环境仍需按本文档独立初始化和配置。

## 1. 服务边界

| 服务 | 模块 | 默认端口              | 说明 |
| --- | --- |-------------------| --- |
| 核心平台 | `zhyc-base-server/zhyc-platform-app` | `8081`            | 组合系统、低代码、工作流、开放平台、采购样板等业务模块 |
| 认证中心 | `zhyc-base-server/zhyc-auth-server` | `8090`            | 提供 OAuth2/OIDC 授权、令牌和 introspection 能力 |
| 开放 API 网关 | `zhyc-base-server/zhyc-openapi-gateway` | 默认 Spring Boot 端口 | 提供 API Key、OAuth2/OIDC、签名、防重放、限流、路由和审计 |
| 后台管理端 | `zhyc-base-vue` | `5173`            | Vue3、Vite、TypeScript、Ant Design Vue 管理端 |
| uni-app 移动端 | `zhyc-base-uniapp` | H5 构建产物           | 移动审批、消息、工作台和个人中心入口 |

## 2. 环境准备

- JDK 21+。
- Node.js 与 npm。
- 默认使用 MySQL 8+；如需多数据库初始化脚本，可使用已生成的 PostgreSQL、Oracle、SQL Server、达梦数据库脚本，并准备核心平台、认证中心和开放 API 网关可访问的数据源。
- Redis 6+；开发环境默认连接 `127.0.0.1:6379`，本地默认无密码，对应 `ZHYC_REDIS_PASSWORD` 留空。Redis 用于核心平台缓存、认证中心会话共享、开放 API 网关限流和 nonce 防重放。
- 后端运行配置通过各服务 `application.yml` 中的 `spring.profiles.active: ${ZHYC_ENV:dev}` 统一控制；默认 `ZHYC_ENV=dev` 加载 `application-dev.yml`，测试和正式环境分别设置 `ZHYC_ENV=test`、`ZHYC_ENV=prod`。本机真实值通过环境变量覆盖，真实密码、Token、私钥不得提交。
- 认证中心需要配置 `ZHYC_AUTH_PLATFORM_TENANT_ID`、`ZHYC_AUTH_PLATFORM_USER_ID`、`ZHYC_AUTH_PLATFORM_USERNAME`，这三个值必须对应核心平台已启用的本地租户和用户，用于把 OAuth2/OIDC 访问令牌映射到 Shiro 主体。
- 后台管理端需要配置 `VITE_AUTH_AUTHORIZATION_ENDPOINT`、`VITE_AUTH_CLIENT_ID`、`VITE_AUTH_REDIRECT_URI`、`VITE_AUTH_SCOPE`，且 `VITE_AUTH_REDIRECT_URI` 必须与认证中心 `ZHYC_AUTH_REDIRECT_URI` 注册值一致。
- 核心平台需要配置 `ZHYC_PLATFORM_AUTH_JWK_SET_URI` 指向认证中心 JWK Set 地址，并配置 `ZHYC_PLATFORM_AUTH_ISSUER` 绑定令牌签发方，用于校验后台管理端和移动端传入的 OAuth2/OIDC Bearer Token；JWK 地址留空时平台不会启用 Bearer Token 过滤器。
- 核心平台需要配置 `ZHYC_PLATFORM_AUTH_TOKEN_URI`、`ZHYC_PLATFORM_AUTH_CLIENT_ID`、`ZHYC_PLATFORM_AUTH_CLIENT_SECRET`，用于后台管理端 OAuth2 授权码回调后的服务端换令牌；`client secret` 只能存放在核心平台服务端环境变量中，禁止写入前端构建变量或静态资源。
- 密钥中心入口位于 `系统管理 -> 密钥管理`，对外统一使用 `secret:<secretCode>` 引用。
- 低代码数据源选择数据库口令时，只能从当前租户启用的 `db_password` 密钥中下拉选择，页面保存的是 `passwordSecretRef=secret:<secretCode>`，不保存明文口令。
- 明文密钥不得写入 profile 样例、种子 SQL、前端构建变量或静态产物；需要真实值时，只通过密钥中心、环境变量或本机 local profile 注入。

## 3. 启动顺序

建议先输出并检查本地启动编排，再按编排步骤启动各服务：

```bash
rtk node scripts/verify-local-environment.mjs --profile dev
```

本地初始化管理员密码哈希必须由平台 Shiro 工具生成，避免手写不可校验的哈希：

```bash
rtk mvn -pl zhyc-platform-app -Dexec.mainClass=com.zhyc.platform.security.PlatformPasswordHashCli exec:java
```

运行前在本机设置 `ZHYC_LOCAL_ADMIN_PASSWORD`，命令只输出 Shiro 哈希值；将输出结果替换 `V2__system_seed.sql` 中的 `replace_with_shiro_password_hash` 后再导入种子数据。

推荐不要修改仓库内种子 SQL，而是把上一步输出的哈希放入本机环境变量 `ZHYC_LOCAL_ADMIN_PASSWORD_HASH`，再生成本机导入文件：

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output /tmp/zhyc-system-seed.local.sql
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --plan
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --check
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-mysql
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-postgresql
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-oracle
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-sqlserver
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-dm
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --check
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --emit-mysql
```

```bash
rtk node scripts/phase1-local-orchestrator.mjs --plan
```

```bash
rtk node scripts/phase1-local-orchestrator.mjs --check
```

### 3.1 两种后端启动方式

首期本地后端同时支持 RTK 命令方式和 IDE 启动类方式。

| 方式 | 使用入口 | 配置来源 | 说明 |
| --- | --- | --- | --- |
| 命令行方式 | `rtk mvn`、`rtk node scripts/run-*.mjs` | `application-dev.yml`、`application-test.yml`、`application-prod.yml` | 适合终端脚本和自动化验证 |
| IDE 启动类方式 | 直接运行 Spring Boot `main` 方法 | `spring.profiles.active=dev/test/prod` | 适合日常断点调试 |

三个后端应用统一使用 Spring Boot profile 区分环境：

- `dev`：本地开发环境。
- `test`：测试环境。
- `prod`：正式环境。

RTK 方式通过 `--env dev`、`--profile dev` 或 `-Dspring-boot.run.profiles=dev` 指定环境；IDE 方式可直接设置环境变量 `ZHYC_ENV=dev`，也可通过 VM options 配置 `-Dspring.profiles.active=dev`。

基础 `application.yml` 已内置环境开关：

```yaml
spring:
  profiles:
    active: ${ZHYC_ENV:dev}
```

开发环境不传值时默认加载 `application-dev.yml`；测试环境设置 `ZHYC_ENV=test`；正式环境设置 `ZHYC_ENV=prod`。`SPRING_PROFILES_ACTIVE`、`-Dspring.profiles.active`、`-Dspring-boot.run.profiles` 仍可按 Spring Boot 原生优先级覆盖该默认值。

三类后端服务均提供以下配置文件：

- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

真实数据库密码、OAuth2 client secret、JWK 私钥、AI Key 不得写入 Git；本机私有值必须通过环境变量注入。

Redis 相关配置项：

| 变量 | 开发默认值 | 说明 |
| --- | --- | --- |
| `ZHYC_REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `ZHYC_REDIS_PORT` | `6379` | Redis 端口 |
| `ZHYC_REDIS_PASSWORD` | 空 | 本地默认无密码 |
| `ZHYC_REDIS_DATABASE` | `0` | Redis 数据库序号 |
| `ZHYC_CACHE_ENABLED` | `true` | 核心平台 Spring Cache 开关 |
| `ZHYC_REDIS_ENABLED` | `true` | 开放 API 网关 Redis 限流和 nonce 开关 |
| `ZHYC_CACHE_PREFIX` | `zhyc` | Redis Key 前缀 |

IDE 启动类配置建议：

| 服务 | Main class | 默认端口 | 必填配置 |
| --- | --- | --- | --- |
| 认证中心 | `com.zhyc.auth.ZhycAuthServerApplication` | `8090` | `ZHYC_AUTH_CLIENT_SECRET`、认证中心数据源、JWK、平台用户映射 |
| 核心平台 | `com.zhyc.platform.ZhycPlatformApplication` | `8081` | `ZHYC_PLATFORM_DATASOURCE_*`、认证中心 JWK Set 和 issuer |
| 开放 API 网关 | `com.zhyc.openapi.ZhycOpenApiGatewayApplication` | `8070` | `SPRING_DATASOURCE_*`、OAuth2 introspection 配置 |

1. 执行 `rtk node scripts/verify-local-environment.mjs --profile dev`，确认 `dev` profile 配置完整且认证配置一致；本机真实值通过环境变量覆盖。
2. 启动 Redis；本地无密码时确认 `ZHYC_REDIS_PASSWORD` 留空。
3. 启动数据库，执行 `rtk node scripts/phase1-db-initializer.mjs --profile dev --plan` 查看认证中心库和核心平台库 DDL 顺序，再执行 `rtk node scripts/phase1-db-initializer.mjs --profile dev --check` 确认首期 DDL 完整；如需手工命令，可用 `rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-mysql`、`rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-postgresql`、`rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-oracle`、`rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-sqlserver` 或 `rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-dm` 输出对应数据库导入示例。
4. 设置本机 `ZHYC_LOCAL_ADMIN_PASSWORD`，运行 `rtk mvn -pl zhyc-platform-app -Dexec.mainClass=com.zhyc.platform.security.PlatformPasswordHashCli exec:java` 生成 Shiro 密码哈希。
5. 执行 `rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan` 查看首期基础种子数据，再执行 `rtk node scripts/phase1-seed-initializer.mjs --profile dev --check` 确认默认租户、管理员占位账号、角色、菜单、字典、参数和模块登记齐全；导入前必须设置本机 `ZHYC_LOCAL_ADMIN_PASSWORD_HASH`，再执行 `rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output /tmp/zhyc-system-seed.local.sql` 生成本机导入文件。
6. 启动认证中心，确认 OAuth2/OIDC 配置可用，且访问令牌包含 `tenant_id`、`user_id`、`preferred_username` 三个核心平台 Claims。
7. 启动核心平台，确认 `ZHYC_PLATFORM_AUTH_JWK_SET_URI` 已指向认证中心 `/oauth2/jwks`，且 `ZHYC_PLATFORM_AUTH_ISSUER` 与认证中心签发方一致，业务模块和 Shiro 权限接口可访问。
8. 启动开放 API 网关，确认 API Key 和 OAuth2/OIDC 两类鉴权路径可用；Redis 开启时限流和 nonce 走 Redis，Redis 异常时回退 JDBC。
9. 启动后台管理端，通过“统一认证登录”进入认证中心，回调后再完成受控令牌交换；本地调试仍可使用运行时上下文维护租户、用户和请求标识。
10. 构建或启动 uni-app H5，用于移动端主链路验收。

## 4. 常用命令

所有命令均使用 `rtk` 前缀。

```bash
rtk node scripts/verify-phase1-contracts.mjs --full
```

```bash
rtk node scripts/verify-auth-integration-readiness.mjs
```

```bash
rtk node scripts/verify-auth-integration-readiness.mjs --live
```

```bash
rtk node scripts/verify-local-environment.mjs --profile dev
```

```bash
rtk mvn -pl zhyc-platform-app -Dexec.mainClass=com.zhyc.platform.security.PlatformPasswordHashCli exec:java
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --plan
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --check
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-mysql
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-postgresql
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-oracle
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-sqlserver
```

```bash
rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-dm
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --check
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --emit-mysql
```

```bash
rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output /tmp/zhyc-system-seed.local.sql
```

```bash
rtk node scripts/phase1-local-orchestrator.mjs --plan
```

```bash
rtk node scripts/phase1-local-orchestrator.mjs --check
```

```bash
rtk npm run build
```

```bash
rtk npm run build:h5
```

```bash
rtk mvn -f zhyc-base-server/pom.xml -DskipTests compile
```

## 5. 验收门禁

- `rtk node scripts/verify-phase1-contracts.mjs --full` 通过。
- 后台管理端生产构建通过。
- uni-app H5 生产构建通过。
- 后端 Maven 编译通过。
- 发布环境不包含明文密钥、Token、真实密码或个人敏感信息。
- 开放 API 网关不得绕过 API Key 或 OAuth2/OIDC 鉴权访问核心平台。
- 认证中心端到端联调前，必须执行 `rtk node scripts/verify-auth-integration-readiness.mjs` 确认配置、端点、前后端回调链路具备联调条件；认证中心启动后再执行 `rtk node scripts/verify-auth-integration-readiness.mjs --live` 探测 discovery、JWK Set、授权端点和 token 端点元数据。
- 本地启动前必须执行 `rtk node scripts/verify-local-environment.mjs --profile dev`，确认 Spring Profile 配置完整、OAuth2 回调一致，认证中心映射到种子租户 `zhyc-platform` 和管理员 `admin`。
- 本地导入种子数据前必须使用 `PlatformPasswordHashCli` 生成管理员密码哈希，并通过 `rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output /tmp/zhyc-system-seed.local.sql` 生成本机导入文件；禁止把明文密码写入 SQL 或运行手册。
- 本地启动前必须执行 `rtk node scripts/phase1-db-initializer.mjs --profile dev --check`，确认认证中心库和核心平台库首期 DDL 文件、执行顺序、幂等建表和租户隔离字段齐全。
- 本地启动前必须执行 `rtk node scripts/phase1-seed-initializer.mjs --profile dev --check`，确认基础租户、管理员占位账号、角色、菜单、字典、参数和模块登记齐全；导入前必须替换密码哈希占位符，禁止使用真实默认口令。
- 本地启动前必须执行 `rtk node scripts/phase1-local-orchestrator.mjs --check`，确认首期启动编排所需目录、脚本、npm 命令和运行材料齐全。

## 6. 回滚说明

- 后端回滚到上一可运行构建包。
- 前端回滚到上一可用静态产物。
- 涉及数据库结构变更时，先执行备份，再按变更脚本准备反向 DDL 或数据修复脚本。
- 回滚后重新执行首期总体验证命令，并检查核心平台、认证中心和开放 API 网关健康状态。
