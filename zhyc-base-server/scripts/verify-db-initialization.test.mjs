/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const scriptsRoot = resolve(fileURLToPath(new URL('.', import.meta.url)));
const serverRoot = resolve(scriptsRoot, '..');
const workspaceRoot = resolve(serverRoot, '..');
const initializerPath = resolve(scriptsRoot, 'phase1-db-initializer.mjs');
const verifierPath = resolve(scriptsRoot, 'verify-db-initialization.mjs');
const runbookPath = resolve(workspaceRoot, 'docs/release/phase1-local-runbook.md');

assert.ok(existsSync(initializerPath), '必须提供首期数据库初始化编排脚本');
assert.ok(existsSync(verifierPath), '必须提供首期数据库初始化门禁脚本');

const initializerSource = readFileSync(initializerPath, 'utf8');
const verifierSource = readFileSync(verifierPath, 'utf8');
const runbookSource = readFileSync(runbookPath, 'utf8');

for (const snippet of ['--plan', '--check', '--emit-mysql', '--emit-postgresql', '--emit-oracle', '--emit-sqlserver',
  '--emit-dm']) {
  assert.match(initializerSource, new RegExp(snippet), `数据库初始化脚本必须支持 ${snippet}`);
}

for (const snippet of [
  'zhyc-auth-server/src/main/resources/db/V1__auth_server_core.sql',
  'zhyc-module-system/src/main/resources/db/V1__system_core.sql',
  'zhyc-module-lowcode/src/main/resources/db/V1__lowcode_core.sql',
  'zhyc-module-workflow/src/main/resources/db/V1__workflow_core.sql',
  'zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql',
  'zhyc-module-openapi/src/main/resources/db/V1__openapi_core.sql',
  'zhyc-module-purchase/src/main/resources/db/V1__purchase_core.sql',
]) {
  assert.match(initializerSource, new RegExp(escapeRegExp(snippet)), `数据库初始化脚本必须登记核心 DDL：${snippet}`);
}

assert.match(verifierSource, /phase1-db-initializer\.mjs/, '数据库初始化门禁必须调用初始化编排脚本');

for (const snippet of [
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --plan',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --check',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-mysql',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-postgresql',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-oracle',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-sqlserver',
  'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-dm',
]) {
  assert.match(runbookSource, new RegExp(escapeRegExp(snippet)), `运行手册必须登记数据库初始化命令：${snippet}`);
}

const planResult = spawnSync('node', ['scripts/phase1-db-initializer.mjs', '--plan'], {
  cwd: serverRoot,
  encoding: 'utf8',
});
assert.equal(planResult.status, 0, planResult.stderr || planResult.stdout);
assert.match(planResult.stdout, /首期数据库初始化计划/, '数据库初始化计划必须有清晰标题');
assert.match(planResult.stdout, /V1__system_core\.sql/, '数据库初始化计划必须输出系统模块 DDL');

const checkResult = spawnSync('node', ['scripts/phase1-db-initializer.mjs', '--check'], {
  cwd: serverRoot,
  encoding: 'utf8',
});
assert.equal(checkResult.status, 0, checkResult.stderr || checkResult.stdout);
assert.match(checkResult.stdout, /首期数据库初始化检查通过/, '数据库初始化检查应通过');

for (const dialect of ['postgresql', 'oracle', 'sqlserver', 'dm']) {
  const buildResult = spawnSync('node', ['scripts/build-database-init-sql.mjs', '--dialect', dialect], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  assert.equal(buildResult.status, 0, buildResult.stderr || buildResult.stdout);
}

const dmInitSqlPath = resolve(workspaceRoot, 'db/init-zhyc-base-v1.dm.sql');
const dmInitSql = readFileSync(dmInitSqlPath, 'utf8');
assert.match(dmInitSql, /PRIMARY KEY \("id"\)/, '达梦初始化脚本主键列必须使用双引号引用');
assert.match(dmInitSql, /CREATE TABLE "act_ru_task"/, '达梦初始化脚本必须包含 Flowable 引擎任务表');
assert.match(dmInitSql, /CREATE TABLE "flw_ru_batch"/, '达梦初始化脚本必须包含 Flowable 批处理表');
assert.match(dmInitSql, /CONSTRAINT "uk_lowcode_ds_tenant_code" UNIQUE \("tenant_id", "code"\)/,
  '达梦初始化脚本唯一约束列必须使用双引号引用');
assert.match(dmInitSql, /CREATE INDEX "idx_lowcode_ds_tenant_enabled" ON "lowcode_data_source" \("tenant_id", "enabled"\);/,
  '达梦初始化脚本索引列必须使用双引号引用');
assert.match(dmInitSql, /CONSTRAINT "fk_lowcode_table_data_source" FOREIGN KEY \("data_source_id"\) REFERENCES "lowcode_data_source" \("id"\)/,
  '达梦初始化脚本外键表名和列名必须使用双引号引用');

for (const [flag, title] of [
  ['--emit-postgresql', '首期数据库初始化 PostgreSQL 命令'],
  ['--emit-oracle', '首期数据库初始化 Oracle 命令'],
  ['--emit-sqlserver', '首期数据库初始化 SQL Server 命令'],
  ['--emit-dm', '首期数据库初始化 达梦数据库 命令'],
]) {
  const emitResult = spawnSync('node', ['scripts/phase1-db-initializer.mjs', flag], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  assert.equal(emitResult.status, 0, emitResult.stderr || emitResult.stdout);
  assert.match(emitResult.stdout, new RegExp(title), `${flag} 必须输出对应数据库初始化命令`);
}

const verifierResult = spawnSync('node', ['scripts/verify-db-initialization.mjs'], {
  cwd: serverRoot,
  encoding: 'utf8',
});
assert.equal(verifierResult.status, 0, verifierResult.stderr || verifierResult.stdout);
assert.match(verifierResult.stdout, /首期数据库初始化门禁通过/, '数据库初始化门禁应通过');

/**
 * 转义正则特殊字符。
 *
 * @param value 待转义文本
 * @returns 可安全拼接到正则的文本
 */
function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
