# ZHYC 快速开发平台宝塔测试环境部署手册

本文档用于在宝塔面板 Linux 服务器部署 ZHYC 测试环境，覆盖认证中心、核心平台、开放 API 网关、后台管理端、Nginx 反向代理、systemd 服务、数据库恢复计划任务和常见故障处理。

> 本文只使用占位符，不记录真实域名、账号、密码、Token、私钥、数据库口令或 Redis 口令。真实值必须存放在服务器环境文件、宝塔数据库配置、密钥管理系统或运维密码库中。

## 1. 部署拓扑

| 组件 | 端口 | 部署方式 | 说明 |
| --- | --- | --- | --- |
| 后台管理端 | `443/80` | 宝塔站点 + Nginx 静态资源 | Vue/Vite 构建产物 |
| 认证中心 | `8090` | `systemd` + Spring Boot jar | OAuth2/OIDC、登录页、令牌签发 |
| 核心平台 | `8081` | `systemd` + Spring Boot jar | 平台业务接口、后台管理接口 |
| 开放 API 网关 | `8070` | `systemd` + Spring Boot jar | 开放 API、OAuth2/API Key 校验 |
| MySQL | `3306` | 宝塔数据库或外部数据库 | 测试库 |
| Redis | `6379` | 宝塔 Redis 或外部 Redis | 缓存、会话、限流和 nonce |

推荐目录：

```text
/www/server/zhyc
├── db
│   └── zhyc_test.sql.zip
├── env
│   └── zhyc-test.env
├── jars
│   ├── zhyc-auth-server-0.0.1-SNAPSHOT.jar
│   ├── zhyc-platform-app-0.0.1-SNAPSHOT.jar
│   └── zhyc-openapi-gateway-0.0.1-SNAPSHOT.jar
├── keys
│   ├── auth-private.pem
│   └── auth-public.pem
├── logs
└── tmp
```

## 2. 部署前准备

### 2.1 宝塔软件

- Nginx。
- MySQL 8 或兼容版本。
- Redis 6 或兼容版本。
- Java 21。确认 Java 可执行文件路径，例如：`/www/server/java/jdk-21.0.2/bin/java`。
- unzip、mysql、mysqldump、systemd。

### 2.2 域名占位符

| 占位符 | 说明 |
| --- | --- |
| `<WEB_DOMAIN>` | 后台管理端访问域名 |
| `<AUTH_DOMAIN>` | 认证中心访问域名 |
| `<OPENAPI_DOMAIN>` | 开放 API 网关访问域名 |
| `<DB_NAME>` | 测试数据库名 |
| `<DB_USERNAME>` | 测试数据库用户 |
| `<DB_PASSWORD>` | 测试数据库密码 |
| `<REDIS_PASSWORD>` | Redis 密码 |
| `<AUTH_CLIENT_SECRET>` | 认证中心 OAuth2 客户端密钥 |
| `<JWK_KEY_ID>` | 认证中心 JWK Key ID |

## 3. 后端构建

本地使用 Java 21 构建后端 jar：

```bash
rtk env JAVA_HOME=<JDK21_HOME> mvn -f zhyc-base-server/pom.xml -DskipTests package
```

构建产物：

```text
zhyc-base-server/zhyc-auth-server/target/zhyc-auth-server-0.0.1-SNAPSHOT.jar
zhyc-base-server/zhyc-platform-app/target/zhyc-platform-app-0.0.1-SNAPSHOT.jar
zhyc-base-server/zhyc-openapi-gateway/target/zhyc-openapi-gateway-0.0.1-SNAPSHOT.jar
```

上传到服务器：

```text
/www/server/zhyc/jars/
```

## 4. 前端构建

后台管理端构建前必须使用测试环境域名，禁止把 `127.0.0.1`、localhost 或本机端口写入构建产物。

```bash
export VITE_AUTH_AUTHORIZATION_ENDPOINT=https://<AUTH_DOMAIN>/oauth2/authorize
export VITE_AUTH_LOGIN_ENDPOINT=https://<AUTH_DOMAIN>/login
export VITE_AUTH_LOGOUT_ENDPOINT=https://<AUTH_DOMAIN>/logout
export VITE_AUTH_POST_LOGOUT_REDIRECT_URI=https://<WEB_DOMAIN>/login?loggedOut=1
export VITE_AUTH_CLIENT_ID=zhyc-auth-client
export VITE_AUTH_REDIRECT_URI=https://<WEB_DOMAIN>/auth/callback
export VITE_AUTH_SCOPE="openid profile"

rtk npm run build
```

上传 `dist` 内容到宝塔站点根目录。

上传后检查静态资源中是否残留本机地址：

```bash
grep -R "127.0.0.1\|localhost" -n <WEB_ROOT> 2>/dev/null | head -20
```

## 5. 环境变量文件

服务器环境文件：

```text
/www/server/zhyc/env/zhyc-test.env
```

示例模板：

```bash
ZHYC_ENV=test

ZHYC_REDIS_HOST=127.0.0.1
ZHYC_REDIS_PORT=6379
ZHYC_REDIS_PASSWORD='<REDIS_PASSWORD>'
ZHYC_REDIS_DATABASE=0
ZHYC_CACHE_ENABLED=true
ZHYC_REDIS_ENABLED=true
ZHYC_CACHE_PREFIX=zhyc:test

ZHYC_PLATFORM_PORT=8081
ZHYC_PLATFORM_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/<DB_NAME>?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
ZHYC_PLATFORM_DATASOURCE_USERNAME=<DB_USERNAME>
ZHYC_PLATFORM_DATASOURCE_PASSWORD='<DB_PASSWORD>'

ZHYC_AUTH_PORT=8090
ZHYC_AUTH_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/<DB_NAME>?useUnicode=true&characterEncoding=utf8&preserveInstants=true&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
ZHYC_AUTH_DATASOURCE_USERNAME=<DB_USERNAME>
ZHYC_AUTH_DATASOURCE_PASSWORD='<DB_PASSWORD>'
ZHYC_AUTH_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver

ZHYC_AUTH_CLIENT_ID=zhyc-auth-client
ZHYC_AUTH_CLIENT_SECRET='<AUTH_CLIENT_SECRET>'
ZHYC_AUTH_ISSUER='https://<AUTH_DOMAIN>'
ZHYC_AUTH_REDIRECT_URI='https://<WEB_DOMAIN>/auth/callback'
ZHYC_AUTH_LOGIN_SUCCESS_REDIRECT_URI='https://<WEB_DOMAIN>/login'
ZHYC_AUTH_FRONTEND_LOGIN_URI='https://<WEB_DOMAIN>/login?authRequest=1'
ZHYC_AUTH_POST_LOGOUT_REDIRECT_URI='https://<WEB_DOMAIN>/login?loggedOut=1'

ZHYC_AUTH_USER_NAME=admin
ZHYC_AUTH_USER_PASSWORD='<LOCAL_FALLBACK_PASSWORD>'
ZHYC_AUTH_PLATFORM_TENANT_ID=zhyc-platform
ZHYC_AUTH_PLATFORM_USER_ID=1
ZHYC_AUTH_PLATFORM_USERNAME=admin

ZHYC_AUTH_JWK_PRIVATE_KEY_PEM='<PRIVATE_KEY_PEM_BASE64_OR_PEM_TEXT>'
ZHYC_AUTH_JWK_PUBLIC_KEY_PEM='<PUBLIC_KEY_PEM_BASE64_OR_PEM_TEXT>'
ZHYC_AUTH_JWK_KEY_ID='<JWK_KEY_ID>'

ZHYC_PLATFORM_AUTH_JWK_SET_URI=https://<AUTH_DOMAIN>/oauth2/jwks
ZHYC_PLATFORM_AUTH_ISSUER=https://<AUTH_DOMAIN>
ZHYC_PLATFORM_AUTH_TOKEN_URI=https://<AUTH_DOMAIN>/oauth2/token
ZHYC_PLATFORM_AUTH_CLIENT_ID=zhyc-auth-client
ZHYC_PLATFORM_AUTH_CLIENT_SECRET='<AUTH_CLIENT_SECRET>'

ZHYC_OPENAPI_PORT=8070
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/<DB_NAME>?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
SPRING_DATASOURCE_USERNAME=<DB_USERNAME>
SPRING_DATASOURCE_PASSWORD='<DB_PASSWORD>'
ZHYC_OPENAPI_OAUTH2_INTROSPECTION_URI=https://<AUTH_DOMAIN>/oauth2/introspect
ZHYC_OPENAPI_OAUTH2_CLIENT_ID=<OPENAPI_CLIENT_ID>
ZHYC_OPENAPI_OAUTH2_CLIENT_SECRET='<OPENAPI_CLIENT_SECRET>'
```

权限建议：

```bash
chmod 600 /www/server/zhyc/env/zhyc-test.env
chmod 700 /www/server/zhyc/env
```

## 6. 认证中心 JWK

生成 RSA 密钥：

```bash
mkdir -p /www/server/zhyc/keys
openssl genrsa -out /www/server/zhyc/keys/auth-private.pem 2048
openssl rsa -in /www/server/zhyc/keys/auth-private.pem -pubout -out /www/server/zhyc/keys/auth-public.pem
chmod 600 /www/server/zhyc/keys/auth-private.pem
chmod 644 /www/server/zhyc/keys/auth-public.pem
```

写入环境文件时必须保留换行。建议先备份环境文件，再通过脚本替换 JWK 相关配置。

## 7. systemd 服务

### 7.1 认证中心

文件：`/etc/systemd/system/zhyc-auth.service`

```ini
[Unit]
Description=ZHYC Auth Server
After=network.target mysql.service redis.service

[Service]
Type=simple
WorkingDirectory=/www/server/zhyc
EnvironmentFile=/www/server/zhyc/env/zhyc-test.env
ExecStart=/www/server/java/jdk-21.0.2/bin/java -jar /www/server/zhyc/jars/zhyc-auth-server-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
StandardOutput=append:/www/server/zhyc/logs/auth.log
StandardError=append:/www/server/zhyc/logs/auth-error.log

[Install]
WantedBy=multi-user.target
```

### 7.2 核心平台

文件：`/etc/systemd/system/zhyc-platform.service`

```ini
[Unit]
Description=ZHYC Platform App
After=network.target mysql.service redis.service zhyc-auth.service

[Service]
Type=simple
WorkingDirectory=/www/server/zhyc
EnvironmentFile=/www/server/zhyc/env/zhyc-test.env
ExecStart=/www/server/java/jdk-21.0.2/bin/java -jar /www/server/zhyc/jars/zhyc-platform-app-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
StandardOutput=append:/www/server/zhyc/logs/platform.log
StandardError=append:/www/server/zhyc/logs/platform-error.log

[Install]
WantedBy=multi-user.target
```

### 7.3 开放 API 网关

文件：`/etc/systemd/system/zhyc-openapi.service`

```ini
[Unit]
Description=ZHYC OpenAPI Gateway
After=network.target mysql.service redis.service zhyc-auth.service

[Service]
Type=simple
WorkingDirectory=/www/server/zhyc
EnvironmentFile=/www/server/zhyc/env/zhyc-test.env
ExecStart=/www/server/java/jdk-21.0.2/bin/java -jar /www/server/zhyc/jars/zhyc-openapi-gateway-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
StandardOutput=append:/www/server/zhyc/logs/openapi.log
StandardError=append:/www/server/zhyc/logs/openapi-error.log

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
systemctl daemon-reload
systemctl enable zhyc-auth zhyc-platform zhyc-openapi
systemctl start zhyc-auth
systemctl start zhyc-platform
systemctl start zhyc-openapi
```

## 8. Nginx 反向代理

### 8.1 后台管理端

宝塔站点根目录指向前端 `dist` 上传目录。Nginx 需要支持 Vue history 路由：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 8.2 认证中心

```nginx
location / {
    proxy_pass http://127.0.0.1:8090;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### 8.3 开放 API 网关

```nginx
location / {
    proxy_pass http://127.0.0.1:8070;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 9. 初始化和验证

启动后检查端口：

```bash
ss -lntp | grep -E '8070|8081|8090'
```

检查服务：

```bash
systemctl status zhyc-auth --no-pager
systemctl status zhyc-platform --no-pager
systemctl status zhyc-openapi --no-pager
```

检查本机 HTTP：

```bash
curl -I http://127.0.0.1:8090/login
curl -I http://127.0.0.1:8081
curl -I http://127.0.0.1:8070
```

检查外网域名：

```bash
curl -I https://<WEB_DOMAIN>
curl -I https://<AUTH_DOMAIN>/login
curl -I https://<OPENAPI_DOMAIN>
```

检查前端是否残留本机地址：

```bash
grep -R "127.0.0.1\|localhost" -n <WEB_ROOT> 2>/dev/null | head -20
```

## 10. 管理员密码重置

认证中心配置了数据库后，登录密码以 `sys_user.password_hash` 为准，`ZHYC_AUTH_USER_PASSWORD` 只作为无数据源时的本地回退密码。

使用平台 jar 内置工具生成 Shiro 密码哈希：

```bash
set -a
source /www/server/zhyc/env/zhyc-test.env
set +a

DB_NAME=$(printf '%s\n' "$ZHYC_AUTH_DATASOURCE_URL" | sed -E 's#^jdbc:mysql://[^/]+/([^?]+).*$#\1#')

HASH=$(ZHYC_LOCAL_ADMIN_PASSWORD='<NEW_ADMIN_PASSWORD>' \
  /www/server/java/jdk-21.0.2/bin/java \
  -Dloader.main=com.zhyc.platform.security.PlatformPasswordHashCli \
  -cp /www/server/zhyc/jars/zhyc-platform-app-0.0.1-SNAPSHOT.jar \
  org.springframework.boot.loader.launch.PropertiesLauncher | tail -n 1)

printf '%s\n' "$HASH" | grep '^\$shiro2\$' || { echo "密码哈希生成失败，停止更新"; exit 1; }

MYSQL_PWD="$ZHYC_AUTH_DATASOURCE_PASSWORD" mysql -u "$ZHYC_AUTH_DATASOURCE_USERNAME" "$DB_NAME" -e "
UPDATE sys_user
SET password_hash='$HASH', status='enabled'
WHERE tenant_id='zhyc-platform' AND username='admin';
"
```

## 11. 每日 08:00 数据库恢复计划任务

适用于测试环境每日固定恢复基线数据。该任务会覆盖测试库，必须先备份当前库。

宝塔计划任务：

```text
任务类型：Shell脚本
任务名称：每天8点恢复 ZHYC 测试数据库
执行周期：每天
执行时间：08:00
```

脚本：

```bash
#!/bin/bash
set -euo pipefail

ENV_FILE="/www/server/zhyc/env/zhyc-test.env"
BACKUP_ZIP="/www/server/zhyc/db/zhyc_test.sql.zip"
WORK_DIR="/www/server/zhyc/db/restore-work"
ROLLBACK_DIR="/www/server/zhyc/db/rollback"
LOG_FILE="/www/server/zhyc/logs/db-restore.log"

mkdir -p "$WORK_DIR" "$ROLLBACK_DIR" "$(dirname "$LOG_FILE")"
exec >> "$LOG_FILE" 2>&1

echo "============================================================"
echo "开始恢复数据库：$(date '+%F %T')"

test -f "$ENV_FILE"
test -f "$BACKUP_ZIP"

set -a
source "$ENV_FILE"
set +a

DB_NAME=$(printf '%s\n' "$ZHYC_PLATFORM_DATASOURCE_URL" | sed -E 's#^jdbc:mysql://[^/]+/([^?]+).*$#\1#')
test -n "$DB_NAME"

systemctl stop zhyc-openapi || true
systemctl stop zhyc-platform || true
systemctl stop zhyc-auth || true

ROLLBACK_SQL="$ROLLBACK_DIR/${DB_NAME}_before_restore_$(date '+%Y%m%d%H%M%S').sql"
MYSQL_PWD="$ZHYC_PLATFORM_DATASOURCE_PASSWORD" mysqldump \
  -u "$ZHYC_PLATFORM_DATASOURCE_USERNAME" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  "$DB_NAME" > "$ROLLBACK_SQL"
gzip -f "$ROLLBACK_SQL"

rm -rf "$WORK_DIR"/*
unzip -o "$BACKUP_ZIP" -d "$WORK_DIR"
RESTORE_SQL=$(find "$WORK_DIR" -type f -name "*.sql" | head -n 1)
test -f "$RESTORE_SQL"

MYSQL_PWD="$ZHYC_PLATFORM_DATASOURCE_PASSWORD" mysql \
  -u "$ZHYC_PLATFORM_DATASOURCE_USERNAME" \
  "$DB_NAME" < "$RESTORE_SQL"

systemctl start zhyc-auth
sleep 10
systemctl start zhyc-platform
sleep 10
systemctl start zhyc-openapi

systemctl is-active zhyc-auth
systemctl is-active zhyc-platform
systemctl is-active zhyc-openapi

echo "数据库恢复完成：$(date '+%F %T')"
```

查看日志：

```bash
tail -n 120 /www/server/zhyc/logs/db-restore.log
```

## 12. 常见故障

### 12.1 systemd `status=203/EXEC`

原因通常是 `ExecStart` 的 Java 路径不存在或无执行权限。

处理：

```bash
ls -l /www/server/java/jdk-21.0.2/bin/java
/www/server/java/jdk-21.0.2/bin/java -version
systemctl daemon-reload
systemctl restart zhyc-auth
```

### 12.2 `UnsupportedClassVersionError`

原因是 jar 使用 Java 21 编译，但服务器运行时低于 Java 21。

处理：将 systemd `ExecStart` 改为 Java 21 路径。

### 12.3 JWK 或私钥解析失败

检查环境变量是否仍是占位符，或者 PEM 换行是否丢失。认证中心启动成功后应能看到 8090 端口监听。

### 12.4 登录页反复刷新

检查：

```bash
grep -n 'ZHYC_AUTH_FRONTEND_LOGIN_URI' /www/server/zhyc/env/zhyc-test.env
```

必须包含：

```text
https://<WEB_DOMAIN>/login?authRequest=1
```

### 12.5 登录后跳转到 `127.0.0.1`

这是前端构建变量错误。重新构建前端，确保 `VITE_AUTH_LOGIN_ENDPOINT` 指向 `https://<AUTH_DOMAIN>/login`，并检查静态资源中没有 `127.0.0.1` 或 localhost。

### 12.6 平台启动时 Flowable 表异常

测试库可能存在大小写重复或半初始化的 Flowable 表。处理前必须备份数据库，再清理异常 `ACT_`、`FLW_` 表，由平台重新初始化。

## 13. 发布门禁

- 后端 jar 使用 Java 21 构建。
- 前端构建产物不包含本机地址、真实密钥和调试地址。
- `zhyc-test.env` 权限为 `600`，不进入 Git。
- Nginx 三个站点均启用 HTTPS。
- 认证中心、核心平台、开放 API 网关均由 systemd 托管。
- 每次数据库恢复前自动备份当前库。
- 登录链路可完成：后台管理端 -> 认证中心 -> 授权回调 -> 平台首页。
- 日志目录可读，错误日志能定位启动失败原因。

## 14. 回滚

后端回滚：

```bash
systemctl stop zhyc-openapi zhyc-platform zhyc-auth
cp /www/server/zhyc/jars-backup/<OLD_AUTH_JAR> /www/server/zhyc/jars/zhyc-auth-server-0.0.1-SNAPSHOT.jar
cp /www/server/zhyc/jars-backup/<OLD_PLATFORM_JAR> /www/server/zhyc/jars/zhyc-platform-app-0.0.1-SNAPSHOT.jar
cp /www/server/zhyc/jars-backup/<OLD_OPENAPI_JAR> /www/server/zhyc/jars/zhyc-openapi-gateway-0.0.1-SNAPSHOT.jar
systemctl start zhyc-auth zhyc-platform zhyc-openapi
```

数据库回滚：

```bash
gunzip -c /www/server/zhyc/db/rollback/<ROLLBACK_SQL>.gz | MYSQL_PWD='<DB_PASSWORD>' mysql -u <DB_USERNAME> <DB_NAME>
```

前端回滚：在宝塔站点目录恢复上一个 `dist` 备份，并清理浏览器缓存或 CDN 缓存。
