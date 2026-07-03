/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { spawnSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const initializerPath = resolve(serverRoot, 'scripts/phase1-db-initializer.mjs');
const runbookPath = resolve(workspaceRoot, 'docs/release/phase1-local-runbook.md');
const consolidatedInitPath = resolve(workspaceRoot, 'db/init-zhyc-base-v1.sql');
const i18nSeedPath = resolve(serverRoot, 'zhyc-module-i18n/src/main/resources/db/V2__i18n_seed.sql');
const errors = [];

if (!existsSync(initializerPath)) {
  errors.push('缺少首期数据库初始化编排脚本：scripts/phase1-db-initializer.mjs');
} else {
  const source = readFileSync(initializerPath, 'utf8');
  for (const snippet of [
    '--plan',
    '--check',
    '--emit-mysql',
    '--emit-postgresql',
    '--emit-oracle',
    '--emit-sqlserver',
    '--emit-dm',
    'V1__auth_server_core.sql',
    'V1__system_core.sql',
    'V1__lowcode_core.sql',
    'V1__workflow_core.sql',
    'V2__flowable_engine_mysql.sql',
    'V1__openapi_core.sql',
  ]) {
    if (!source.includes(snippet)) {
      errors.push(`数据库初始化编排脚本缺少关键内容：${snippet}`);
    }
  }
}

if (!existsSync(runbookPath)) {
  errors.push('缺少首期本地运行手册：docs/release/phase1-local-runbook.md');
} else {
  const runbook = readFileSync(runbookPath, 'utf8');
  for (const snippet of [
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --plan',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --check',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-mysql',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-postgresql',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-oracle',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-sqlserver',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --emit-dm',
  ]) {
    if (!runbook.includes(snippet)) {
      errors.push(`运行手册缺少数据库初始化命令：${snippet}`);
    }
  }
}

if (!existsSync(consolidatedInitPath)) {
  errors.push('缺少根目录数据库初始化脚本：db/init-zhyc-base-v1.sql');
} else {
  const initSql = readFileSync(consolidatedInitPath, 'utf8').toLowerCase();
  for (const snippet of [
    'create table if not exists sys_secret',
    'create table if not exists act_ru_task',
    'create table if not exists flw_ru_batch',
    "'system-secret', '密钥管理', 'menu', '/system/secrets', 'system/secret/index', 'system:secret:query'",
    "'system-secret-create', '密钥新增', 'button', null, null, 'system:secret:create'",
    "'system-secret-copy-ref', '复制引用', 'button', null, null, 'system:secret:copy-ref'",
  ]) {
    if (!initSql.includes(snippet)) {
      errors.push(`根目录初始化脚本缺少密钥中心内容：${snippet}`);
    }
  }
  for (const snippet of [
    'insert into i18n_message',
    "'zhyc-platform', 'zh-cn', 'platform.name', 'zhyc 快速开发平台', 'enabled'",
    "'zhyc-platform', 'en-us', 'platform.name', 'zhyc rapid development platform', 'enabled'",
    "'i18n-message-save', '词条保存', 'button', null, null, 'i18n:message:save'",
    "'i18n-message-resolve', '词条解析', 'button', null, null, 'i18n:message:resolve'",
  ]) {
    if (!initSql.includes(snippet)) {
      errors.push(`根目录初始化脚本缺少国际化初始化内容：${snippet}`);
    }
  }
}

for (const dialectInitPath of [
  'db/init-zhyc-base-v1.postgresql.sql',
  'db/init-zhyc-base-v1.oracle.sql',
  'db/init-zhyc-base-v1.sqlserver.sql',
  'db/init-zhyc-base-v1.dm.sql',
]) {
  const absoluteDialectInitPath = resolve(workspaceRoot, dialectInitPath);
  if (!existsSync(absoluteDialectInitPath)) {
    errors.push(`缺少主流数据库初始化脚本：${dialectInitPath}`);
    continue;
  }
  const dialectInitSql = readFileSync(absoluteDialectInitPath, 'utf8').toLowerCase();
  if (!dialectInitSql.includes('create table')) {
    errors.push(`主流数据库初始化脚本缺少建表语句：${dialectInitPath}`);
  }
  if (!dialectInitSql.includes('act_ru_task')) {
    errors.push(`主流数据库初始化脚本缺少 Flowable 引擎任务表：${dialectInitPath}`);
  }
  if (!dialectInitSql.includes('tenant_id')) {
    errors.push(`主流数据库初始化脚本缺少租户字段：${dialectInitPath}`);
  }
}

if (!existsSync(i18nSeedPath)) {
  errors.push('缺少国际化词条初始化脚本：zhyc-module-i18n/src/main/resources/db/V2__i18n_seed.sql');
} else {
  const seedSql = readFileSync(i18nSeedPath, 'utf8').toLowerCase();
  for (const snippet of [
    'insert into i18n_message',
    "'zhyc-platform', 'zh-cn', 'platform.name', 'zhyc 快速开发平台', 'enabled'",
    "'zhyc-platform', 'en-us', 'platform.name', 'zhyc rapid development platform', 'enabled'",
    "'menu.i18n.message'",
    "'button.save'",
    'on duplicate key update',
  ]) {
    if (!seedSql.includes(snippet)) {
      errors.push(`国际化词条初始化脚本缺少内容：${snippet}`);
    }
  }
}

if (errors.length === 0) {
  const planResult = spawnSync('node', ['scripts/phase1-db-initializer.mjs', '--plan'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (planResult.status !== 0 || !planResult.stdout.includes('首期数据库初始化计划')) {
    errors.push(`数据库初始化计划输出失败：${planResult.stderr || planResult.stdout}`);
  }
}

if (errors.length === 0) {
  const checkResult = spawnSync('node', ['scripts/phase1-db-initializer.mjs', '--check'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (checkResult.status !== 0 || !checkResult.stdout.includes('首期数据库初始化检查通过')) {
    errors.push(`数据库初始化检查失败：${checkResult.stderr || checkResult.stdout}`);
  }
}

if (errors.length > 0) {
  console.error('首期数据库初始化门禁失败。');
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

console.log('首期数据库初始化门禁通过。');
