/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, mkdtempSync, readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';

const scriptsRoot = resolve(process.cwd(), 'scripts');
const seedInitializerPath = resolve(scriptsRoot, 'phase1-seed-initializer.mjs');
const verifierPath = resolve(scriptsRoot, 'verify-seed-initialization.mjs');
const seedSqlPath = resolve(process.cwd(), 'zhyc-module-system/src/main/resources/db/V2__system_seed.sql');
const runbookPath = resolve(process.cwd(), '../docs/release/phase1-local-runbook.md');

assert.ok(existsSync(seedInitializerPath), '必须提供首期种子数据初始化编排脚本');
assert.ok(existsSync(verifierPath), '必须提供首期种子数据初始化门禁脚本');
assert.ok(existsSync(seedSqlPath), '必须提供系统模块首期种子数据 SQL');

const seedInitializerSource = readFileSync(seedInitializerPath, 'utf8');
const verifierSource = readFileSync(verifierPath, 'utf8');
const seedSqlSource = readFileSync(seedSqlPath, 'utf8');
const runbookSource = readFileSync(runbookPath, 'utf8');

for (const snippet of ['--plan', '--check', '--emit-mysql', '--materialize', 'ZHYC_LOCAL_ADMIN_PASSWORD_HASH']) {
  assert.match(seedInitializerSource, new RegExp(snippet), `种子数据初始化脚本必须支持 ${snippet}`);
}

for (const snippet of [
  'INSERT IGNORE INTO sys_tenant',
  'INSERT IGNORE INTO sys_user',
  'INSERT IGNORE INTO sys_role',
  'INTO sys_menu',
  'ON DUPLICATE KEY UPDATE',
  'INSERT IGNORE INTO sys_role_menu',
  'INSERT IGNORE INTO sys_module',
  'tenant_id',
  'zhyc-platform',
  'admin',
  'replace_with_shiro_password_hash',
]) {
  assert.match(seedSqlSource, new RegExp(escapeRegExp(snippet)), `系统种子 SQL 必须包含：${snippet}`);
}

assert.match(verifierSource, /phase1-seed-initializer\.mjs/, '种子数据门禁必须调用初始化编排脚本');

for (const snippet of [
  'rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan',
  'rtk node scripts/phase1-seed-initializer.mjs --profile dev --check',
  'rtk node scripts/phase1-seed-initializer.mjs --profile dev --emit-mysql',
  'rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output',
]) {
  assert.match(runbookSource, new RegExp(escapeRegExp(snippet)), `运行手册必须登记种子数据命令：${snippet}`);
}

const planResult = spawnSync('node', ['scripts/phase1-seed-initializer.mjs', '--plan'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(planResult.status, 0, planResult.stderr || planResult.stdout);
assert.match(planResult.stdout, /首期种子数据初始化计划/, '种子数据初始化计划必须有清晰标题');

const checkResult = spawnSync('node', ['scripts/phase1-seed-initializer.mjs', '--check'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(checkResult.status, 0, checkResult.stderr || checkResult.stdout);
assert.match(checkResult.stdout, /首期种子数据初始化检查通过/, '种子数据初始化检查应通过');

const tempRoot = mkdtempSync(join(tmpdir(), 'zhyc-seed-materialize-'));
const materializedSqlPath = join(tempRoot, 'V2__system_seed.local.sql');
const passwordHash = '$shiro1$SHA-256$500000$localSaltValue$localPasswordHashValueForTestOnly';
const materializeResult = spawnSync('node', [
  'scripts/phase1-seed-initializer.mjs',
  '--materialize',
  '--output',
  materializedSqlPath,
], {
  cwd: process.cwd(),
  env: {
    ...process.env,
    ZHYC_LOCAL_ADMIN_PASSWORD_HASH: passwordHash,
  },
  encoding: 'utf8',
});
assert.equal(materializeResult.status, 0, materializeResult.stderr || materializeResult.stdout);
const materializedSql = readFileSync(materializedSqlPath, 'utf8');
assert.match(materializedSql, new RegExp(escapeRegExp(passwordHash)), '物化 SQL 必须写入本地 Shiro 密码哈希');
assert.doesNotMatch(materializedSql, /replace_with_shiro_password_hash/, '物化 SQL 不得保留密码哈希占位符');

const missingHashResult = spawnSync('node', [
  'scripts/phase1-seed-initializer.mjs',
  '--materialize',
  '--output',
  join(tempRoot, 'missing.sql'),
], {
  cwd: process.cwd(),
  env: Object.fromEntries(Object.entries(process.env).filter(([key]) => key !== 'ZHYC_LOCAL_ADMIN_PASSWORD_HASH')),
  encoding: 'utf8',
});
assert.notEqual(missingHashResult.status, 0, '缺少 ZHYC_LOCAL_ADMIN_PASSWORD_HASH 时必须拒绝物化 SQL');
assert.match(`${missingHashResult.stderr}\n${missingHashResult.stdout}`, /ZHYC_LOCAL_ADMIN_PASSWORD_HASH/,
  '缺少密码哈希时必须提示环境变量名称');

const verifierResult = spawnSync('node', ['scripts/verify-seed-initialization.mjs'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(verifierResult.status, 0, verifierResult.stderr || verifierResult.stdout);
assert.match(verifierResult.stdout, /首期种子数据初始化门禁通过/, '种子数据初始化门禁应通过');

/**
 * 转义正则特殊字符。
 *
 * @param value 待转义文本
 * @returns 可安全拼接到正则的文本
 */
function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
