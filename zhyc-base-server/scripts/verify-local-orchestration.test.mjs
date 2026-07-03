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

const scriptsRoot = resolve(process.cwd(), 'scripts');
const orchestratorPath = resolve(scriptsRoot, 'phase1-local-orchestrator.mjs');
const verifierPath = resolve(scriptsRoot, 'verify-local-orchestration.mjs');
const runPlatformLocalPath = resolve(scriptsRoot, 'run-platform-local.mjs');
const mavenLocalSettingsPath = resolve(scriptsRoot, 'maven-local-settings.xml');

assert.ok(existsSync(orchestratorPath), '必须提供首期本地启动编排脚本');
assert.ok(existsSync(verifierPath), '必须提供首期本地启动编排门禁脚本');
assert.ok(existsSync(runPlatformLocalPath), '必须提供核心平台本地启动脚本');
assert.ok(existsSync(mavenLocalSettingsPath), '必须提供本地启动专用 Maven settings');

const orchestratorSource = readFileSync(orchestratorPath, 'utf8');
const verifierSource = readFileSync(verifierPath, 'utf8');
const runPlatformLocalSource = readFileSync(runPlatformLocalPath, 'utf8');
const mavenLocalSettingsSource = readFileSync(mavenLocalSettingsPath, 'utf8');

assert.match(orchestratorSource, /--plan/, '本地启动编排脚本必须支持 --plan 输出启动步骤');
assert.match(orchestratorSource, /--check/, '本地启动编排脚本必须支持 --check 校验本地前置条件');
assert.match(orchestratorSource, /Spring Profile 配置检查/, '本地启动编排脚本必须包含 Spring Profile 配置检查');
assert.match(orchestratorSource, /verify-local-environment\.mjs --profile dev/,
  '本地启动编排脚本必须包含 Spring Profile 配置检查命令');
assert.match(orchestratorSource, /认证中心/, '本地启动编排脚本必须包含认证中心启动步骤');
assert.match(orchestratorSource, /数据库初始化前置检查/, '本地启动编排脚本必须包含数据库初始化前置检查');
assert.match(orchestratorSource, /phase1-db-initializer\.mjs --profile dev --check/,
  '本地启动编排脚本必须包含数据库初始化检查命令');
assert.match(orchestratorSource, /种子数据初始化前置检查/, '本地启动编排脚本必须包含种子数据初始化前置检查');
assert.match(orchestratorSource, /phase1-seed-initializer\.mjs --profile dev --check/,
  '本地启动编排脚本必须包含种子数据初始化检查命令');
assert.match(orchestratorSource, /核心平台/, '本地启动编排脚本必须包含核心平台启动步骤');
assert.match(orchestratorSource, /后台管理端/, '本地启动编排脚本必须包含后台管理端启动步骤');
assert.match(orchestratorSource, /uni-app/, '本地启动编排脚本必须包含 uni-app 骨架验收步骤');
assert.match(orchestratorSource, /verify-auth-integration-readiness\.mjs --live/,
  '本地启动编排脚本必须包含认证中心实时联调检查');
assert.match(verifierSource, /phase1-local-orchestrator\.mjs/,
  '本地启动编排门禁必须检查编排脚本');
assert.match(runPlatformLocalSource, /resolveMavenSettingsPath/,
  '核心平台本地启动脚本必须解析受控 Maven settings');
assert.match(runPlatformLocalSource, /--settings/,
  '核心平台本地启动脚本必须显式传入 Maven settings，避免本机 mirror 劫持');
assert.match(runPlatformLocalSource, /maven-local-settings\.xml/,
  '核心平台本地启动脚本默认必须使用本地启动专用 Maven settings');
assert.match(runPlatformLocalSource, /--maven-settings/,
  '核心平台本地启动脚本必须支持显式覆盖 Maven settings');
assert.match(mavenLocalSettingsSource, /https:\/\/repo\.maven\.apache\.org\/maven2/,
  '本地启动专用 Maven settings 必须默认使用 Maven Central HTTPS');
assert.doesNotMatch(mavenLocalSettingsSource, /maven\.aliyun|nexus-aliyun|<url>\s*http:\/\//,
  '本地启动专用 Maven settings 不得默认使用不稳定的 HTTP 镜像');

const planResult = spawnSync('node', ['scripts/phase1-local-orchestrator.mjs', '--plan'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(planResult.status, 0, planResult.stderr || planResult.stdout);
assert.match(planResult.stdout, /首期本地启动编排计划/, '启动编排计划必须有清晰标题');
assert.match(planResult.stdout, /rtk node scripts\/verify-local-environment\.mjs --profile dev/,
  '启动编排计划必须包含 Spring Profile 配置检查命令');
assert.match(planResult.stdout, /rtk node scripts\/verify-phase1-contracts\.mjs/,
  '启动编排计划必须包含首期契约验证命令');
assert.match(planResult.stdout, /rtk node scripts\/phase1-db-initializer\.mjs --profile dev --check/,
  '启动编排计划必须包含数据库初始化检查命令');
assert.match(planResult.stdout, /rtk node scripts\/phase1-seed-initializer\.mjs --profile dev --check/,
  '启动编排计划必须包含种子数据初始化检查命令');

const verifierResult = spawnSync('node', ['scripts/verify-local-orchestration.mjs'], {
  cwd: process.cwd(),
  encoding: 'utf8',
});
assert.equal(verifierResult.status, 0, verifierResult.stderr || verifierResult.stdout);
assert.match(verifierResult.stdout, /首期本地启动编排门禁通过/, '本地启动编排门禁应通过');
