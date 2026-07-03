/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

// 默认模式只跑首期轻量契约；--full 额外跑前端生产构建，并在 JDK 21+ 时跑后端 Maven 编译。
const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const fullMode = process.argv.includes('--full');

const checks = [
  {
    name: '技术基线门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-build-baseline.test.mjs'],
  },
  {
    name: '技术基线门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-build-baseline.mjs'],
  },
  {
    name: '模块边界门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-module-boundaries.test.mjs'],
  },
  {
    name: '模块边界门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-module-boundaries.mjs'],
  },
  {
    name: '模块元数据质量门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-module-metadata-quality.test.mjs'],
  },
  {
    name: '模块元数据质量门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-module-metadata-quality.mjs'],
  },
  {
    name: '低代码生成器契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-lowcode-generator.mjs'],
  },
  {
    name: '低代码元数据契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-lowcode-metadata.mjs'],
  },
  {
    name: '低代码数据库方言契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-lowcode-dialect.mjs'],
  },
  {
    name: '低代码服务业务异常门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-lowcode-service-business-errors.test.mjs'],
  },
  {
    name: '低代码服务业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-lowcode-service-business-errors.mjs'],
  },
  {
    name: 'SaaS 租户首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-saas-tenancy.mjs'],
  },
  {
    name: '工作流首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-workflow-phase1.mjs'],
  },
  {
    name: '工作流运行状态门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-workflow-runtime-status.test.mjs'],
  },
  {
    name: '工作流运行状态门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-workflow-runtime-status.mjs'],
  },
  {
    name: 'Flowable 边界门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-flowable-boundary.test.mjs'],
  },
  {
    name: 'Flowable 边界门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-flowable-boundary.mjs'],
  },
  {
    name: '工作流服务业务异常门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-workflow-service-business-errors.test.mjs'],
  },
  {
    name: '工作流服务业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-workflow-service-business-errors.mjs'],
  },
  {
    name: '采购首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-purchase-phase1.mjs'],
  },
  {
    name: '采购服务业务异常门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-purchase-service-business-errors.test.mjs'],
  },
  {
    name: '采购服务业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-purchase-service-business-errors.mjs'],
  },
  {
    name: 'OpenAPI 首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-openapi-phase1.mjs'],
  },
  {
    name: 'OpenAPI 服务业务异常门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-openapi-service-business-errors.test.mjs'],
  },
  {
    name: 'OpenAPI 服务业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-openapi-service-business-errors.mjs'],
  },
  {
    name: 'OpenAPI 网关运行时契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-openapi-gateway.mjs'],
  },
  {
    name: '消息首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-message-phase1.mjs'],
  },
  {
    name: '文件中心首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-file-phase1.mjs'],
  },
  {
    name: '内容管理首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-cms-phase1.mjs'],
  },
  {
    name: '在线作业首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-job-phase1.mjs'],
  },
  {
    name: '全文检索首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-search-phase1.mjs'],
  },
  {
    name: '安全与认证首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-security-phase1.mjs'],
  },
  {
    name: '认证中心联调前置检查测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-auth-integration-readiness.test.mjs'],
  },
  {
    name: '认证中心联调前置检查',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-auth-integration-readiness.mjs'],
  },
  {
    name: '本地环境变量门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-local-environment.test.mjs'],
  },
  {
    name: '本地环境变量样例门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-local-environment.mjs', '--example'],
  },
  {
    name: 'Shiro 密码哈希生成工具测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-password-hash-helper.test.mjs'],
  },
  {
    name: 'Shiro 密码哈希生成工具门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-password-hash-helper.mjs'],
  },
  {
    name: '首期数据库初始化编排测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-db-initialization.test.mjs'],
  },
  {
    name: '首期数据库初始化门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-db-initialization.mjs'],
  },
  {
    name: '首期种子数据初始化编排测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-seed-initialization.test.mjs'],
  },
  {
    name: '首期种子数据初始化门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-seed-initialization.mjs'],
  },
  {
    name: '首期本地启动编排测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-local-orchestration.test.mjs'],
  },
  {
    name: '首期本地启动编排门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-local-orchestration.mjs'],
  },
  {
    name: '系统 Controller 业务异常契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-system-controller-business-errors.mjs'],
  },
  {
    name: '系统服务业务异常门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-system-service-business-errors.test.mjs'],
  },
  {
    name: '系统服务业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-system-service-business-errors.mjs'],
  },
  {
    name: '后台 Controller 权限门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-admin-controller-permissions.test.mjs'],
  },
  {
    name: '后台 Controller 权限门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-admin-controller-permissions.mjs'],
  },
  {
    name: '后端权限元数据门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-backend-permission-metadata.test.mjs'],
  },
  {
    name: '后端权限元数据门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-backend-permission-metadata.mjs'],
  },
  {
    name: 'Controller 统一响应门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-controller-response-contract.test.mjs'],
  },
  {
    name: 'Controller 统一响应门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-controller-response-contract.mjs'],
  },
  {
    name: 'Controller 业务异常门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-controller-business-errors.mjs'],
  },
  {
    name: 'RequestBody 空请求体门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-request-body-null-guard.test.mjs'],
  },
  {
    name: 'RequestBody 空请求体门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-request-body-null-guard.mjs'],
  },
  {
    name: '统一分页响应契约测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-page-result-contract.test.mjs'],
  },
  {
    name: '统一分页响应契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-page-result-contract.mjs'],
  },
  {
    name: '前端统一分页响应契约测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-page-result-contract.test.mjs'],
  },
  {
    name: '前端统一分页响应契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-page-result-contract.mjs'],
  },
  {
    name: '前端路由权限声明门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-route-permissions.test.mjs'],
  },
  {
    name: '前端路由权限声明门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-route-permissions.mjs'],
  },
  {
    name: '数据库 DDL 注释门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-sql-comments.mjs'],
  },
  {
    name: 'SQL 安全门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-sql-safety.test.mjs'],
  },
  {
    name: 'SQL 安全门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-sql-safety.mjs'],
  },
  {
    name: 'Java 中文注释门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-java-comments.test.mjs'],
  },
  {
    name: 'Java 中文注释门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-java-comments.mjs'],
  },
  {
    name: '生产源码占位门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-no-production-placeholders.test.mjs'],
  },
  {
    name: '生产源码占位门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-no-production-placeholders.mjs'],
  },
  {
    name: '硬编码上下文门禁测试',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-no-hardcoded-context.test.mjs'],
  },
  {
    name: '硬编码上下文门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-no-hardcoded-context.mjs'],
  },
  {
    name: '敏感配置明文门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-sensitive-config.mjs'],
  },
  {
    name: '首期交付运行材料门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-release-readiness.mjs'],
  },
  {
    name: '前端安全请求头门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-security-headers.mjs'],
  },
  {
    name: '前端 API 签名门禁',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-frontend-api-signatures.mjs'],
  },
  {
    name: '可视化首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-visual-phase1.mjs'],
  },
  {
    name: '国际化首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-i18n-phase1.mjs'],
  },
  {
    name: '系统监控首期契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-monitor-phase1.mjs'],
  },
  {
    name: 'Agent 与规范形成契约',
    cwd: serverRoot,
    command: ['node', 'scripts/verify-agent-standards.mjs'],
  },
  {
    name: '后台管理端骨架契约',
    cwd: resolve(workspaceRoot, 'zhyc-base-vue'),
    command: ['npm', 'run', 'verify:shell', '--silent'],
  },
  {
    name: 'uni-app 移动端骨架契约',
    cwd: resolve(workspaceRoot, 'zhyc-base-uniapp'),
    command: ['npm', 'run', 'verify:shell', '--silent'],
  },
];

if (fullMode) {
  checks.push(
    {
      name: '后台管理端生产构建',
      cwd: resolve(workspaceRoot, 'zhyc-base-vue'),
      command: ['npm', 'run', 'build', '--silent'],
    },
    {
      name: 'uni-app H5 生产构建',
      cwd: resolve(workspaceRoot, 'zhyc-base-uniapp'),
      command: ['npm', 'run', 'build:h5', '--silent'],
    },
  );
}

const failedChecks = [];
const skippedChecks = [];

checks.unshift({
  name: '首期验证编排自检',
  cwd: serverRoot,
  command: ['node', 'scripts/verify-phase1-contracts.test.mjs'],
});

if (fullMode) {
  const javaVersion = detectJavaMajorVersion();
  if (javaVersion >= 21) {
    const mavenCommand = resolveMavenCommand();
    checks.push({
      name: '后端 Maven 编译',
      cwd: serverRoot,
      command: [mavenCommand, '-DskipTests', 'compile'],
    });
  } else {
    skippedChecks.push(`后端 Maven 编译：当前 JDK ${javaVersion || '未知'}，项目要求 JDK 21+`);
  }
}

for (const check of checks) {
  console.log(`\n[开始] ${check.name}`);
  const result = spawnSync(check.command[0], check.command.slice(1), {
    cwd: check.cwd,
    encoding: 'utf8',
  });

  if (result.stdout) {
    process.stdout.write(result.stdout);
  }
  if (result.stderr) {
    process.stderr.write(result.stderr);
  }

  if (result.status === 0) {
    console.log(`[通过] ${check.name}`);
  } else {
    failedChecks.push(`${check.name}，退出码：${result.status ?? '未知'}${result.error ? `，错误：${result.error.message}` : ''}`);
    console.error(`[失败] ${check.name}`);
  }
}

if (failedChecks.length > 0) {
  console.error('\n首期总体验证失败。');
  for (const failedCheck of failedChecks) {
    console.error(`- ${failedCheck}`);
  }
  process.exit(1);
}

if (skippedChecks.length > 0) {
  console.warn('\n首期总体验证存在跳过项。');
  for (const skippedCheck of skippedChecks) {
    console.warn(`- ${skippedCheck}`);
  }
}

console.log('\n首期总体验证通过。');

/**
 * 检测当前 Java 主版本号。
 *
 * @returns Java 主版本号，无法识别时返回 0
 */
function detectJavaMajorVersion() {
  const result = spawnSync('java', ['-version'], {
    cwd: serverRoot,
    encoding: 'utf8',
  });
  const versionOutput = `${result.stderr || ''}\n${result.stdout || ''}`;
  const legacyMatch = versionOutput.match(/version "1\.(\d+)/);
  if (legacyMatch) {
    return Number.parseInt(legacyMatch[1], 10);
  }
  const modernMatch = versionOutput.match(/version "(\d+)/);
  if (modernMatch) {
    return Number.parseInt(modernMatch[1], 10);
  }
  return 0;
}

/**
 * 解析首期 full 验证可用的 Maven 命令。
 *
 * <p>优先使用显式环境变量和项目内 Maven Wrapper；没有配置时回退到 PATH 中的 mvn。</p>
 *
 * @returns 可交给 spawnSync 执行的 Maven 命令路径
 */
function resolveMavenCommand() {
  if (process.env.MAVEN_CMD) {
    return process.env.MAVEN_CMD;
  }

  const mavenWrapper = resolve(serverRoot, 'mvnw');
  if (existsSync(mavenWrapper)) {
    return mavenWrapper;
  }

  return 'mvn';
}
