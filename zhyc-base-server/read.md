# 管理后台服务说明

## 端口与用途

- 默认端口：`8081`
- 工程目录：`zhyc-base-server`
- 启动模块：`zhyc-platform-app`

管理后台服务是平台业务 API 主入口，面向后台管理 Vue 和 uni-app 用户端提供系统管理、SaaS 租户、低代码、工作流、开放平台管理、采购样板和扩展模块能力。

## 技术栈

- Spring Boot 4
- Java 21
- Apache Shiro 2.2.1
- MyBatis
- MySQL
- Redis Cache
- Maven

首期采用模块化单体方式运行，认证中心和开放 API 网关保持独立边界。业务数据按共享库共享表模式设计，通过 `tenant_id` 做租户隔离。

## 启动前置

- MySQL 已启动，`zhyc-base-v1` 已完成初始化。
- Redis 已启动，本地默认 `127.0.0.1:6379` 且无密码；如使用其他地址，通过 `ZHYC_REDIS_HOST`、`ZHYC_REDIS_PORT`、`ZHYC_REDIS_PASSWORD` 覆盖。
- 后端应用运行配置已按 `application-dev.yml`、`application-test.yml`、`application-prod.yml` 区分环境，基础 `application.yml` 通过 `spring.profiles.active: ${ZHYC_ENV:dev}` 控制默认环境。
- 认证中心 `8090` 启动后，核心平台可通过 `ZHYC_PLATFORM_AUTH_JWK_SET_URI` 校验 OAuth2/OIDC Token。

## 启动方式

进入后端工程目录：

```bash
rtk node scripts/run-platform-local.mjs --profile dev
```

如果默认端口 `8081` 已被占用，可临时指定其他端口：

```bash
rtk node scripts/run-platform-local.mjs --profile dev --port 18081
```

启动前先检查 profile 和端口配置：

```bash
rtk node scripts/run-platform-local.mjs --profile dev --check
```

如果直接使用 Maven 或在 IDE 中启动，必须指定 Spring Boot profile：

- 统一环境开关：`ZHYC_ENV=dev/test/prod`
- RTK/Maven：`-Dspring-boot.run.profiles=dev`
- IDE VM options：`-Dspring.profiles.active=dev`

命令行启动示例：

```bash
rtk mvn -pl zhyc-platform-app -am -DskipTests install
rtk mvn -pl zhyc-platform-app org.springframework.boot:spring-boot-maven-plugin:4.1.0:run -Dspring-boot.run.mainClass=com.zhyc.platform.ZhycPlatformApplication -Dspring-boot.run.profiles=dev
```

## 验证方式

```bash
rtk curl -I http://127.0.0.1:8081
```

启动编排检查：

```bash
rtk node scripts/phase1-local-orchestrator.mjs --check
```
