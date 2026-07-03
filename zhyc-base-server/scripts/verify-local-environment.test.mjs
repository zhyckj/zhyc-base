/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';

const scriptPath = resolve(process.cwd(), 'scripts/verify-local-environment.mjs');

assert.ok(existsSync(scriptPath), '必须提供首期本地环境变量检查脚本');

const source = readFileSync(scriptPath, 'utf8');

for (const snippet of ['--example', '--strict', '--file', '--profile', '--env', 'replace_with_', 'VITE_AUTH_REDIRECT_URI']) {
  assert.match(source, new RegExp(escapeRegExp(snippet)), `本地环境变量检查脚本必须包含：${snippet}`);
}

const exampleResult = spawnSync('node', ['scripts/verify-local-environment.mjs', '--example'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(exampleResult.status, 0, exampleResult.stderr || exampleResult.stdout);
assert.match(exampleResult.stdout, /首期本地环境变量样例检查通过/, '环境变量样例检查应通过');

const profileResult = spawnSync('node', ['scripts/verify-local-environment.mjs', '--profile', 'dev'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(profileResult.status, 0, profileResult.stderr || profileResult.stdout);
assert.match(profileResult.stdout, /首期 Spring Profile 配置检查通过：dev/, 'Spring Profile 配置检查应通过');

const environmentResult = spawnSync('node', ['scripts/verify-local-environment.mjs', '--env', 'dev'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(environmentResult.status, 0, environmentResult.stderr || environmentResult.stdout);
assert.match(environmentResult.stdout, /首期 Spring Profile 配置检查通过：dev/, '统一环境开关检查应通过');

const tempRoot = mkdtempSync(join(tmpdir(), 'zhyc-local-env-'));
const validEnvFile = join(tempRoot, 'phase1.env');
writeFileSync(validEnvFile, buildEnvContent({
  ZHYC_PLATFORM_DATASOURCE_USERNAME: 'platform_user',
  ZHYC_PLATFORM_DATASOURCE_PASSWORD: 'platform_password_value',
  ZHYC_AUTH_CLIENT_SECRET: 'auth_client_secret_value',
  ZHYC_AUTH_USER_NAME: 'auth_admin',
  ZHYC_AUTH_USER_PASSWORD: 'auth_password_value',
  ZHYC_AUTH_PLATFORM_TENANT_ID: 'zhyc-platform',
  ZHYC_AUTH_PLATFORM_USER_ID: '1',
  ZHYC_AUTH_PLATFORM_USERNAME: 'admin',
  ZHYC_AUTH_DATASOURCE_USERNAME: 'auth_db_user',
  ZHYC_AUTH_DATASOURCE_PASSWORD: 'auth_db_password_value',
  ZHYC_AUTH_JWK_PRIVATE_KEY_PEM: 'local_private_key_pem',
  ZHYC_AUTH_JWK_PUBLIC_KEY_PEM: 'local_public_key_pem',
  ZHYC_AUTH_JWK_KEY_ID: 'local-key-id',
  SPRING_DATASOURCE_USERNAME: 'gateway_user',
  SPRING_DATASOURCE_PASSWORD: 'gateway_password_value',
  ZHYC_OPENAPI_OAUTH2_CLIENT_ID: 'openapi_client',
  ZHYC_OPENAPI_OAUTH2_CLIENT_SECRET: 'openapi_client_secret_value',
}));

const strictValidResult = spawnSync('node', ['scripts/verify-local-environment.mjs', '--strict', '--file', validEnvFile], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(strictValidResult.status, 0, strictValidResult.stderr || strictValidResult.stdout);
assert.match(strictValidResult.stdout, /首期本地环境变量严格检查通过/, '严格环境变量检查应通过');

const invalidEnvFile = join(tempRoot, 'invalid.env');
writeFileSync(invalidEnvFile, buildEnvContent({
  ZHYC_PLATFORM_AUTH_CLIENT_ID: 'platform-client',
  VITE_AUTH_CLIENT_ID: 'vue-client',
  ZHYC_PLATFORM_AUTH_CLIENT_SECRET: 'replace_with_platform_auth_client_secret',
}));

const strictInvalidResult = spawnSync('node', ['scripts/verify-local-environment.mjs', '--strict', '--file', invalidEnvFile], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.notEqual(strictInvalidResult.status, 0, '严格模式必须拒绝占位符和不一致的 clientId');
assert.match(`${strictInvalidResult.stderr}\n${strictInvalidResult.stdout}`, /仍为占位符|clientId 不一致/,
  '严格模式失败原因必须指出占位符或 clientId 不一致');

/**
 * 构建测试环境变量内容。
 *
 * @param overrides 覆盖键值
 * @returns 环境变量文件内容
 */
function buildEnvContent(overrides = {}) {
  const base = {
    SPRING_PROFILES_ACTIVE: 'local',
    ZHYC_PLATFORM_DATASOURCE_URL: 'jdbc:mysql://127.0.0.1:3306/zhyc_platform',
    ZHYC_PLATFORM_DATASOURCE_USERNAME: 'platform_user',
    ZHYC_PLATFORM_DATASOURCE_PASSWORD: 'platform_password_value',
    ZHYC_PLATFORM_AUTH_JWK_SET_URI: 'http://127.0.0.1:8090/oauth2/jwks',
    ZHYC_PLATFORM_AUTH_ISSUER: 'http://127.0.0.1:8090',
    ZHYC_PLATFORM_AUTH_TOKEN_URI: 'http://127.0.0.1:8090/oauth2/token',
    ZHYC_PLATFORM_AUTH_CLIENT_ID: 'zhyc-auth-client',
    ZHYC_PLATFORM_AUTH_CLIENT_SECRET: 'auth_client_secret_value',
    ZHYC_AUTH_CLIENT_ID: 'zhyc-auth-client',
    ZHYC_AUTH_CLIENT_SECRET: 'auth_client_secret_value',
    ZHYC_AUTH_REDIRECT_URI: 'http://127.0.0.1:5173/auth/callback',
    ZHYC_AUTH_ISSUER: 'http://127.0.0.1:8090',
    ZHYC_AUTH_USER_NAME: 'auth_admin',
    ZHYC_AUTH_USER_PASSWORD: 'auth_password_value',
    ZHYC_AUTH_PLATFORM_TENANT_ID: 'zhyc-platform',
    ZHYC_AUTH_PLATFORM_USER_ID: '1',
    ZHYC_AUTH_PLATFORM_USERNAME: 'admin',
    ZHYC_AUTH_DATASOURCE_URL: 'jdbc:mysql://127.0.0.1:3306/zhyc_auth',
    ZHYC_AUTH_DATASOURCE_USERNAME: 'auth_db_user',
    ZHYC_AUTH_DATASOURCE_PASSWORD: 'auth_db_password_value',
    ZHYC_AUTH_DATASOURCE_DRIVER: 'com.mysql.cj.jdbc.Driver',
    ZHYC_AUTH_JWK_PRIVATE_KEY_PEM: 'local_private_key_pem',
    ZHYC_AUTH_JWK_PUBLIC_KEY_PEM: 'local_public_key_pem',
    ZHYC_AUTH_JWK_KEY_ID: 'local-key-id',
    SPRING_DATASOURCE_URL: 'jdbc:mysql://127.0.0.1:3306/zhyc_platform',
    SPRING_DATASOURCE_USERNAME: 'gateway_user',
    SPRING_DATASOURCE_PASSWORD: 'gateway_password_value',
    ZHYC_OPENAPI_OAUTH2_INTROSPECTION_URI: 'http://127.0.0.1:8090/oauth2/introspect',
    ZHYC_OPENAPI_OAUTH2_CLIENT_ID: 'openapi_client',
    ZHYC_OPENAPI_OAUTH2_CLIENT_SECRET: 'openapi_client_secret_value',
    VITE_AUTH_AUTHORIZATION_ENDPOINT: 'http://127.0.0.1:8090/oauth2/authorize',
    VITE_AUTH_CLIENT_ID: 'zhyc-auth-client',
    VITE_AUTH_REDIRECT_URI: 'http://127.0.0.1:5173/auth/callback',
    VITE_AUTH_SCOPE: 'openid profile',
    ...overrides,
  };
  return Object.entries(base)
    .map(([key, value]) => `${key}=${value}`)
    .join('\n');
}

/**
 * 转义正则特殊字符。
 *
 * @param value 待转义文本
 * @returns 可安全拼接到正则的文本
 */
function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
