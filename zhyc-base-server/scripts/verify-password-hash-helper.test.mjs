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

const helperPath = resolve(process.cwd(), 'zhyc-platform-app/src/main/java/com/zhyc/platform/security/PlatformPasswordHashCli.java');
const helperTestPath = resolve(process.cwd(), 'zhyc-platform-app/src/test/java/com/zhyc/platform/security/PlatformPasswordHashCliTest.java');
const verifierPath = resolve(process.cwd(), 'scripts/verify-password-hash-helper.mjs');
const pomPath = resolve(process.cwd(), 'zhyc-platform-app/pom.xml');
const runbookPath = resolve(process.cwd(), '../docs/release/phase1-local-runbook.md');

assert.ok(existsSync(helperPath), '必须提供 Shiro 密码哈希生成 CLI');
assert.ok(existsSync(helperTestPath), '必须提供 Shiro 密码哈希生成 CLI 测试');
assert.ok(existsSync(verifierPath), '必须提供 Shiro 密码哈希生成工具门禁');

const helperSource = readFileSync(helperPath, 'utf8');
const helperTestSource = readFileSync(helperTestPath, 'utf8');
const verifierSource = readFileSync(verifierPath, 'utf8');
const pomSource = readFileSync(pomPath, 'utf8');
const runbookSource = readFileSync(runbookPath, 'utf8');

for (const snippet of [
  'DefaultPasswordService',
  'ZHYC_LOCAL_ADMIN_PASSWORD',
  'encryptPassword',
  'MIN_PASSWORD_LENGTH',
]) {
  assert.match(helperSource, new RegExp(snippet), `密码哈希生成 CLI 必须包含：${snippet}`);
}

assert.match(helperTestSource, /passwordsMatch/, '密码哈希生成 CLI 测试必须校验 Shiro 可匹配');
assert.match(verifierSource, /PlatformPasswordHashCli/, '密码哈希生成工具门禁必须检查 CLI');
assert.match(pomSource, /exec-maven-plugin/, '平台应用必须声明 exec-maven-plugin 便于运行 CLI');
assert.match(runbookSource, /ZHYC_LOCAL_ADMIN_PASSWORD/, '运行手册必须说明本地管理员密码环境变量');
assert.match(runbookSource, /PlatformPasswordHashCli/, '运行手册必须登记密码哈希生成 CLI 命令');

const verifierResult = spawnSync('node', ['scripts/verify-password-hash-helper.mjs'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(verifierResult.status, 0, verifierResult.stderr || verifierResult.stdout);
assert.match(verifierResult.stdout, /Shiro 密码哈希生成工具门禁通过/, '密码哈希生成工具门禁应通过');
