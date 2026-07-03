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
const orchestratorPath = resolve(serverRoot, 'scripts/phase1-local-orchestrator.mjs');
const runPlatformLocalPath = resolve(serverRoot, 'scripts/run-platform-local.mjs');
const mavenLocalSettingsPath = resolve(serverRoot, 'scripts/maven-local-settings.xml');
const runbookPath = resolve(workspaceRoot, 'docs/release/phase1-local-runbook.md');
const errors = [];

if (!existsSync(orchestratorPath)) {
  errors.push('缺少首期本地启动编排脚本：scripts/phase1-local-orchestrator.mjs');
} else {
  const source = readFileSync(orchestratorPath, 'utf8');
  for (const snippet of [
    '--plan',
    '--check',
    'Spring Profile 配置检查',
    'verify-local-environment.mjs --profile dev',
    '认证中心',
    '数据库初始化前置检查',
    'phase1-db-initializer.mjs --profile dev --check',
    '种子数据初始化前置检查',
    'phase1-seed-initializer.mjs --profile dev --check',
    '核心平台',
    '开放 API 网关',
    '后台管理端',
    'uni-app',
    'verify-auth-integration-readiness.mjs --live',
  ]) {
    if (!source.includes(snippet)) {
      errors.push(`本地启动编排脚本缺少关键内容：${snippet}`);
    }
  }
}

if (!existsSync(runPlatformLocalPath)) {
  errors.push('缺少核心平台本地启动脚本：scripts/run-platform-local.mjs');
} else {
  const source = readFileSync(runPlatformLocalPath, 'utf8');
  for (const snippet of [
    'resolveMavenSettingsPath',
    '--settings',
    'maven-local-settings.xml',
    '--maven-settings',
  ]) {
    if (!source.includes(snippet)) {
      errors.push(`核心平台本地启动脚本缺少 Maven settings 隔离能力：${snippet}`);
    }
  }
}

if (!existsSync(mavenLocalSettingsPath)) {
  errors.push('缺少本地启动专用 Maven settings：scripts/maven-local-settings.xml');
} else {
  const source = readFileSync(mavenLocalSettingsPath, 'utf8');
  if (!source.includes('https://repo.maven.apache.org/maven2')) {
    errors.push('本地启动专用 Maven settings 必须默认使用 Maven Central HTTPS');
  }
  if (/maven\.aliyun|nexus-aliyun|<url>\s*http:\/\//.test(source)) {
    errors.push('本地启动专用 Maven settings 不得默认使用不稳定的 HTTP 镜像');
  }
}

if (!existsSync(runbookPath)) {
  errors.push('缺少首期本地运行手册：docs/release/phase1-local-runbook.md');
} else {
  const runbook = readFileSync(runbookPath, 'utf8');
  for (const snippet of [
    'rtk node scripts/verify-local-environment.mjs --profile dev',
    'PlatformPasswordHashCli',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --plan',
    'rtk node scripts/phase1-db-initializer.mjs --profile dev --check',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --plan',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --check',
    'rtk node scripts/phase1-seed-initializer.mjs --profile dev --materialize --output',
    'rtk node scripts/phase1-local-orchestrator.mjs --plan',
    'rtk node scripts/phase1-local-orchestrator.mjs --check',
  ]) {
    if (!runbook.includes(snippet)) {
      errors.push(`运行手册缺少本地启动编排命令：${snippet}`);
    }
  }
}

if (errors.length === 0) {
  const planResult = spawnSync('node', ['scripts/phase1-local-orchestrator.mjs', '--plan'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (planResult.status !== 0 || !planResult.stdout.includes('首期本地启动编排计划')) {
    errors.push(`本地启动编排计划输出失败：${planResult.stderr || planResult.stdout}`);
  }
}

if (errors.length === 0) {
  const checkResult = spawnSync('node', ['scripts/phase1-local-orchestrator.mjs', '--check'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  if (checkResult.status !== 0 || !checkResult.stdout.includes('首期本地启动编排检查通过')) {
    errors.push(`本地启动编排检查失败：${checkResult.stderr || checkResult.stdout}`);
  }
}

if (errors.length > 0) {
  console.error('首期本地启动编排门禁失败。');
  errors.forEach((error) => console.error(`- ${error}`));
  process.exit(1);
}

console.log('首期本地启动编排门禁通过。');
