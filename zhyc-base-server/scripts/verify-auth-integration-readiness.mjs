/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from 'node:http';
import { request as requestHttps } from 'node:https';
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const liveMode = process.argv.includes('--live');
const errors = [];

const requiredFiles = [
  'docs/release/phase1.env.example',
  'docs/release/phase1-local-runbook.md',
  'zhyc-base-server/zhyc-auth-server/src/main/java/com/zhyc/auth/config/AuthorizationServerConfig.java',
  'zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/auth/PlatformOAuthTokenController.java',
  'zhyc-base-vue/src/utils/adminOAuth.ts',
  'zhyc-base-vue/src/api/auth/oauth.ts',
  'zhyc-base-vue/src/views/auth/callback.vue',
];

const requiredSnippets = [
  ['docs/release/phase1.env.example', 'ZHYC_AUTH_REDIRECT_URI=http://127.0.0.1:5173/auth/callback'],
  ['docs/release/phase1.env.example', 'ZHYC_AUTH_ISSUER=http://127.0.0.1:8090'],
  ['docs/release/phase1.env.example', 'ZHYC_PLATFORM_AUTH_JWK_SET_URI=http://127.0.0.1:8090/oauth2/jwks'],
  ['docs/release/phase1.env.example', 'ZHYC_PLATFORM_AUTH_TOKEN_URI=http://127.0.0.1:8090/oauth2/token'],
  ['docs/release/phase1.env.example', 'ZHYC_PLATFORM_AUTH_CLIENT_ID=zhyc-auth-client'],
  ['docs/release/phase1.env.example', 'ZHYC_PLATFORM_AUTH_CLIENT_SECRET='],
  ['docs/release/phase1.env.example', 'VITE_AUTH_AUTHORIZATION_ENDPOINT=http://127.0.0.1:8090/oauth2/authorize'],
  ['docs/release/phase1.env.example', 'VITE_AUTH_CLIENT_ID=zhyc-auth-client'],
  ['docs/release/phase1.env.example', 'VITE_AUTH_REDIRECT_URI=http://127.0.0.1:5173/auth/callback'],
  ['docs/release/phase1-local-runbook.md', 'rtk node scripts/verify-auth-integration-readiness.mjs'],
  ['docs/release/phase1-local-runbook.md', 'rtk node scripts/verify-auth-integration-readiness.mjs --live'],
  ['zhyc-base-server/zhyc-auth-server/src/main/java/com/zhyc/auth/config/AuthorizationServerConfig.java', 'oidc(Customizer.withDefaults())'],
  ['zhyc-base-server/zhyc-auth-server/src/main/java/com/zhyc/auth/config/AuthorizationServerConfig.java', 'AuthorizationServerSettings'],
  ['zhyc-base-server/zhyc-auth-server/src/main/java/com/zhyc/auth/config/AuthorizationServerConfig.java', 'OAuth2TokenType.ACCESS_TOKEN'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/auth/PlatformOAuthTokenController.java', '@PostMapping("/token")'],
  ['zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/auth/PlatformOAuthTokenController.java', '@PostMapping("/refresh")'],
  ['zhyc-base-vue/src/utils/adminOAuth.ts', 'oauth2/authorize'],
  ['zhyc-base-vue/src/api/auth/oauth.ts', '/api/auth/oauth2/token'],
  ['zhyc-base-vue/src/api/auth/oauth.ts', '/api/auth/oauth2/refresh'],
  ['zhyc-base-vue/src/views/auth/callback.vue', 'saveAdminRuntimeContext'],
];

for (const file of requiredFiles) {
  if (!existsSync(resolve(workspaceRoot, file))) {
    errors.push(`缺少联调文件：${file}`);
  }
}

for (const [file, snippet] of requiredSnippets) {
  const absolutePath = resolve(workspaceRoot, file);
  if (!existsSync(absolutePath)) {
    continue;
  }
  const content = readFileSync(absolutePath, 'utf8');
  if (!content.includes(snippet)) {
    errors.push(`缺少联调关键内容：${file} -> ${snippet}`);
  }
}

if (errors.length === 0 && liveMode) {
  await verifyLiveAuthEndpoints();
}

if (errors.length > 0) {
  console.error('认证中心联调前置检查失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(liveMode ? '认证中心联调前置检查通过，实时端点可访问。' : '认证中心联调前置检查通过。');

/**
 * 实时探测认证中心 OAuth2/OIDC 关键端点。
 */
async function verifyLiveAuthEndpoints() {
  const issuer = readEnvExampleValue('ZHYC_AUTH_ISSUER') || 'http://127.0.0.1:8090';
  const expectedEndpoints = [
    `${issuer}/.well-known/openid-configuration`,
    `${issuer}/.well-known/oauth-authorization-server`,
    `${issuer}/oauth2/jwks`,
  ];

  for (const endpoint of expectedEndpoints) {
    const response = await httpGet(endpoint);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      errors.push(`认证中心实时端点不可用：${endpoint} -> HTTP ${response.statusCode}`);
      continue;
    }
    if (endpoint.endsWith('/.well-known/openid-configuration')) {
      assertDiscoveryMetadata(endpoint, response.body);
    }
  }
}

/**
 * 校验 OIDC discovery 元数据。
 *
 * @param endpoint 元数据端点
 * @param body 响应正文
 */
function assertDiscoveryMetadata(endpoint, body) {
  try {
    const metadata = JSON.parse(body);
    const requiredMetadataKeys = ['issuer', 'authorization_endpoint', 'token_endpoint', 'jwks_uri'];
    for (const key of requiredMetadataKeys) {
      if (!metadata[key]) {
        errors.push(`认证中心 discovery 元数据缺少 ${key}：${endpoint}`);
      }
    }
    if (!String(metadata.authorization_endpoint || '').includes('/oauth2/authorize')) {
      errors.push(`认证中心授权端点不符合预期：${metadata.authorization_endpoint || '空'}`);
    }
    if (!String(metadata.token_endpoint || '').includes('/oauth2/token')) {
      errors.push(`认证中心 token 端点不符合预期：${metadata.token_endpoint || '空'}`);
    }
    if (!String(metadata.jwks_uri || '').includes('/oauth2/jwks')) {
      errors.push(`认证中心 JWK Set 端点不符合预期：${metadata.jwks_uri || '空'}`);
    }
  } catch (error) {
    errors.push(`认证中心 discovery 元数据不是合法 JSON：${endpoint}`);
  }
}

/**
 * 从环境变量样例中读取配置值。
 *
 * @param key 环境变量名称
 * @returns 配置值，未配置时返回空字符串
 */
function readEnvExampleValue(key) {
  const envFile = resolve(workspaceRoot, 'docs/release/phase1.env.example');
  const line = readFileSync(envFile, 'utf8')
    .split(/\r?\n/)
    .find((item) => item.startsWith(`${key}=`));
  return line ? line.slice(key.length + 1).trim() : '';
}

/**
 * 发起 HTTP GET 请求。
 *
 * @param url 目标 URL
 * @returns HTTP 状态码和响应正文
 */
function httpGet(url) {
  return new Promise((resolvePromise) => {
    const client = url.startsWith('https://') ? requestHttps : request;
    const req = client(url, { method: 'GET', timeout: 3000 }, (res) => {
      const chunks = [];
      res.on('data', (chunk) => chunks.push(chunk));
      res.on('end', () => {
        resolvePromise({
          statusCode: res.statusCode || 0,
          body: Buffer.concat(chunks).toString('utf8'),
        });
      });
    });
    req.on('timeout', () => {
      req.destroy();
      resolvePromise({ statusCode: 0, body: '' });
    });
    req.on('error', () => {
      resolvePromise({ statusCode: 0, body: '' });
    });
    req.end();
  });
}
