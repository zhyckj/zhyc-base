/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || process.cwd());
const workflowSourceRoot = resolve(rootDir, 'zhyc-module-workflow/src/main/java');
const allowedFile = 'zhyc-module-workflow/src/main/java/com/zhyc/workflow/constant/WorkflowRuntimeStatus.java';
const allowedFilePath = resolve(rootDir, allowedFile);
const statusLiteralPattern = /["'](RUNNING|TODO|APPROVED|REJECTED|REVOKED)["']/g;
const violations = [];
const requiredEnumSnippets = [
  'RUNNING("RUNNING", "流程实例运行中")',
  'TODO("TODO", "任务待处理")',
  'APPROVED("APPROVED", "任务已审批通过")',
  'REJECTED("REJECTED", "任务已驳回")',
  'REVOKED("REVOKED", "流程或任务已撤回")',
  'public String getDescription()',
  'public static WorkflowRuntimeStatus fromCode(String code)',
  'throw new IllegalArgumentException("不支持的工作流运行状态编码: " + code)',
];

if (!existsSync(workflowSourceRoot)) {
  console.error(`工作流生产源码目录不存在: ${workflowSourceRoot}`);
  process.exit(1);
}

if (!existsSync(allowedFilePath)) {
  violations.push(`${allowedFile} -> 缺少工作流运行状态枚举文件`);
} else {
  const enumContent = readFileSync(allowedFilePath, 'utf8');
  for (const snippet of requiredEnumSnippets) {
    if (!enumContent.includes(snippet)) {
      violations.push(`${allowedFile} -> 缺少枚举约束片段: ${snippet}`);
    }
  }
}

for (const javaFile of listJavaFiles(workflowSourceRoot)) {
  const relativePath = relative(rootDir, javaFile).split('\\').join('/');
  if (relativePath === allowedFile) {
    continue;
  }
  const lines = readFileSync(javaFile, 'utf8').split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    for (const match of lines[index].matchAll(statusLiteralPattern)) {
      violations.push(`${relativePath}:${index + 1} -> 运行状态 ${match[1]} 必须从 WorkflowRuntimeStatus 获取`);
    }
  }
}

if (violations.length > 0) {
  console.error('工作流运行状态门禁失败。运行状态编码必须集中在 WorkflowRuntimeStatus，禁止在生产 Java 中散落魔法字符串：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('工作流运行状态门禁通过。');

/**
 * 递归列出 Java 源码文件。
 *
 * @param root 当前扫描路径
 * @returns Java 源码文件列表
 */
function listJavaFiles(root) {
  const rootStat = statSync(root);
  if (rootStat.isFile()) {
    return root.endsWith('.java') ? [root] : [];
  }
  return readdirSync(root, { withFileTypes: true }).flatMap((entry) => {
    const path = join(root, entry.name);
    if (entry.isDirectory()) {
      return entry.name === 'target' ? [] : listJavaFiles(path);
    }
    return entry.isFile() && entry.name.endsWith('.java') ? [path] : [];
  });
}
