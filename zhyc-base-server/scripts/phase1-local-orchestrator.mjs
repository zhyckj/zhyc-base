/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const mode = process.argv.includes('--check') ? 'check' : 'plan';

const startupSteps = [
  {
    name: 'Spring Profile 配置检查',
    command: 'rtk node scripts/verify-local-environment.mjs --profile dev',
    cwd: 'zhyc-base-server',
    note: '确认 dev profile 配置完整、安全，并提示真实敏感值必须通过环境变量注入。',
  },
  {
    name: '数据库初始化前置检查',
    command: 'rtk node scripts/phase1-db-initializer.mjs --profile dev --check',
    cwd: 'zhyc-base-server',
    note: '确认认证中心库和核心平台库的首期 DDL 文件、执行顺序、幂等建表和租户隔离字段齐全。',
  },
  {
    name: '种子数据初始化前置检查',
    command: 'rtk node scripts/phase1-seed-initializer.mjs --profile dev --check',
    cwd: 'zhyc-base-server',
    note: '确认默认租户、管理员占位账号、角色、菜单、字典、参数和模块登记的本地种子数据齐全。',
  },
  {
    name: '首期静态契约验证',
    command: 'rtk node scripts/verify-phase1-contracts.mjs',
    cwd: 'zhyc-base-server',
    note: '确认后端、后台管理端、uni-app、认证中心和开放 API 网关的首期契约未破坏。',
  },
  {
    name: '认证中心联调前置检查',
    command: 'rtk node scripts/verify-auth-integration-readiness.mjs',
    cwd: 'zhyc-base-server',
    note: '确认 OAuth2/OIDC 配置、回调地址、BFF 换令牌和刷新链路具备联调条件。',
  },
  {
    name: '认证中心启动',
    command: 'rtk mvn -pl zhyc-auth-server spring-boot:run',
    cwd: 'zhyc-base-server',
    note: '默认端口 8090，启动前需要准备认证中心数据源和 JWK 环境变量。',
  },
  {
    name: '认证中心实时端点检查',
    command: 'rtk node scripts/verify-auth-integration-readiness.mjs --live',
    cwd: 'zhyc-base-server',
    note: '认证中心启动后探测 discovery、OAuth metadata 和 JWK Set。',
  },
  {
    name: '核心平台启动',
    command: 'rtk node scripts/run-platform-local.mjs',
    cwd: 'zhyc-base-server',
    note: '默认端口 8081，负责组合系统、低代码、工作流、开放平台和样板业务模块。',
  },
  {
    name: '开放 API 网关启动',
    command: 'rtk mvn -pl zhyc-openapi-gateway spring-boot:run',
    cwd: 'zhyc-base-server',
    note: '用于验证 API Key、OAuth2/OIDC、签名、防重放、限流和调用审计。',
  },
  {
    name: '后台管理端启动',
    command: 'rtk npm run dev',
    cwd: 'zhyc-base-vue',
    note: '默认端口 5173，通过统一认证登录进入后台管理端。',
  },
  {
    name: 'uni-app 骨架验收',
    command: 'rtk npm run verify:shell',
    cwd: 'zhyc-base-uniapp',
    note: '确认移动端 H5、小程序、App 后续生成入口仍符合首期骨架契约。',
  },
];

if (mode === 'check') {
  runLocalCheck();
} else {
  printStartupPlan();
}

/**
 * 输出首期本地启动编排计划。
 */
function printStartupPlan() {
  console.log('首期本地启动编排计划');
  startupSteps.forEach((step, index) => {
    console.log(`${index + 1}. ${step.name}`);
    console.log(`   目录：${step.cwd}`);
    console.log(`   命令：${step.command}`);
    console.log(`   说明：${step.note}`);
  });
}

/**
 * 校验本地启动编排所需文件和命令声明。
 */
function runLocalCheck() {
  const errors = [];
  const requiredPaths = [
    'docs/release/phase1-local-runbook.md',
    'zhyc-base-server/pom.xml',
    'zhyc-base-server/zhyc-auth-server/pom.xml',
    'zhyc-base-server/zhyc-platform-app/pom.xml',
    'zhyc-base-server/zhyc-openapi-gateway/pom.xml',
    'zhyc-base-server/scripts/verify-phase1-contracts.mjs',
    'zhyc-base-server/scripts/verify-local-environment.mjs',
    'zhyc-base-server/scripts/run-platform-local.mjs',
    'zhyc-base-server/scripts/verify-password-hash-helper.mjs',
    'zhyc-base-server/zhyc-platform-app/src/main/java/com/zhyc/platform/security/PlatformPasswordHashCli.java',
    'zhyc-base-server/scripts/phase1-db-initializer.mjs',
    'zhyc-base-server/scripts/verify-db-initialization.mjs',
    'zhyc-base-server/scripts/phase1-seed-initializer.mjs',
    'zhyc-base-server/scripts/verify-seed-initialization.mjs',
    'zhyc-base-server/zhyc-module-system/src/main/resources/db/V2__system_seed.sql',
    'zhyc-base-server/scripts/verify-auth-integration-readiness.mjs',
    'zhyc-base-vue/package.json',
    'zhyc-base-uniapp/package.json',
  ];

  for (const file of requiredPaths) {
    if (!existsSync(resolve(workspaceRoot, file))) {
      errors.push(`缺少本地启动文件：${file}`);
    }
  }

  assertPackageScript('zhyc-base-vue/package.json', 'dev', errors);
  assertPackageScript('zhyc-base-vue/package.json', 'verify:shell', errors);
  assertPackageScript('zhyc-base-uniapp/package.json', 'verify:shell', errors);
  assertRunbookCommands(errors);

  if (errors.length > 0) {
    console.error('首期本地启动编排检查失败。');
    errors.forEach((error) => console.error(`- ${error}`));
    process.exit(1);
  }

  console.log('首期本地启动编排检查通过。');
}

/**
 * 校验 package.json 是否声明指定脚本。
 *
 * @param packagePath package.json 路径
 * @param scriptName 脚本名称
 * @param errors 错误收集器
 */
function assertPackageScript(packagePath, scriptName, errors) {
  const absolutePath = resolve(workspaceRoot, packagePath);
  if (!existsSync(absolutePath)) {
    return;
  }
  const packageJson = JSON.parse(readFileSync(absolutePath, 'utf8'));
  if (!packageJson.scripts?.[scriptName]) {
    errors.push(`缺少 npm 脚本：${packagePath} -> ${scriptName}`);
  }
}

/**
 * 校验运行手册是否登记本地启动编排命令。
 *
 * @param errors 错误收集器
 */
function assertRunbookCommands(errors) {
  const runbookPath = resolve(workspaceRoot, 'docs/release/phase1-local-runbook.md');
  if (!existsSync(runbookPath)) {
    return;
  }
  const content = readFileSync(runbookPath, 'utf8');
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
    if (!content.includes(snippet)) {
      errors.push(`运行手册缺少本地启动编排命令：${snippet}`);
    }
  }
}
