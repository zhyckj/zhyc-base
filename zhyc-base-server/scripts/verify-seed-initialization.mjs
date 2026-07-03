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
const seedInitializerPath = resolve(serverRoot, 'scripts/phase1-seed-initializer.mjs');
const seedSqlPath = resolve(serverRoot, 'zhyc-module-system/src/main/resources/db/V2__system_seed.sql');
const runbookPath = resolve(workspaceRoot, 'docs/release/phase1-local-runbook.md');
const errors = [];

if (!existsSync(seedInitializerPath)) {
  errors.push('缺少首期种子数据初始化编排脚本：scripts/phase1-seed-initializer.mjs');
} else {
  const source = readFileSync(seedInitializerPath, 'utf8');
  for (const snippet of [
    '--plan',
    '--check',
    '--emit-mysql',
    '--materialize',
    '--output',
    'ZHYC_LOCAL_ADMIN_PASSWORD_HASH',
    'V2__system_seed.sql',
  ]) {
    if (!source.includes(snippet)) {
      errors.push(`种子数据初始化编排脚本缺少关键内容：${snippet}`);
    }
  }
}

if (!existsSync(seedSqlPath)) {
  errors.push('缺少系统模块首期种子数据 SQL：zhyc-module-system/src/main/resources/db/V2__system_seed.sql');
}

if (!existsSync(runbookPath)) {
  errors.push('缺少首期本地运行手册：docs/release/phase1-local-runbook.md');
} else {
  const runbook = readFileSync(runbookPath, 'utf8');
  for (const snippet of [
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --check',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --emit-mysql',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output',
  ]) {
    if (!runbook.includes(snippet)) {
      errors.push(`运行手册缺少种子数据初始化命令：${snippet}`);
    }
  }
}

if (errors.length === 0) {
  const planResult = spawnSync('node', ['scripts/phase1-seed-initializer.mjs', '--plan'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (planResult.status !== 0 || !planResult.stdout.includes('首期种子数据初始化计划')) {
    errors.push(`种子数据初始化计划输出失败：${planResult.stderr || planResult.stdout}`);
  }
}

if (errors.length === 0) {
  const checkResult = spawnSync('node', ['scripts/phase1-seed-initializer.mjs', '--check'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (checkResult.status !== 0 || !checkResult.stdout.includes('首期种子数据初始化检查通过')) {
    errors.push(`种子数据初始化检查失败：${checkResult.stderr || checkResult.stdout}`);
  }
}

if (errors.length > 0) {
  console.error('首期种子数据初始化门禁失败。');
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

console.log('首期种子数据初始化门禁通过。');
