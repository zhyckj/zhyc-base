/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

const scriptPath = resolve(process.cwd(), 'scripts/verify-auth-integration-readiness.mjs');

assert.ok(existsSync(scriptPath), '必须提供认证中心端到端联调前置检查脚本');

const source = readFileSync(scriptPath, 'utf8');

assert.match(source, /--live/, '联调前置检查脚本必须支持 --live 探测本地认证中心端点');
assert.match(source, /oauth2\/authorize/, '联调前置检查脚本必须校验授权端点');
assert.match(source, /oauth2\/token/, '联调前置检查脚本必须校验 token 端点');
assert.match(source, /oauth2\/jwks/, '联调前置检查脚本必须校验 JWK Set 端点');
assert.match(source, /ZHYC_PLATFORM_AUTH_CLIENT_SECRET/, '联调前置检查脚本必须确认服务端 client secret 配置入口');
assert.match(source, /VITE_AUTH_REDIRECT_URI/, '联调前置检查脚本必须确认后台回调地址配置入口');

const result = spawnSync('node', ['scripts/verify-auth-integration-readiness.mjs'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});

assert.equal(result.status, 0, result.stderr || result.stdout);
assert.match(result.stdout, /认证中心联调前置检查通过/, '静态联调前置检查应在当前工程通过');
