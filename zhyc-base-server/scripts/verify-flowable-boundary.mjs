/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || process.cwd());
const flowableImportPattern = /^\s*import\s+org\.flowable\./;
const allowedModulePath = '/zhyc-module-workflow/';
const violations = [];

if (!existsSync(rootDir)) {
  console.error(`扫描根目录不存在: ${rootDir}`);
  process.exit(1);
}

for (const file of listJavaFiles(rootDir)) {
  const normalizedPath = file.split('\\').join('/');
  if (!normalizedPath.includes('/src/main/java/')) {
    continue;
  }
  if (normalizedPath.includes(allowedModulePath)) {
    continue;
  }
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    if (flowableImportPattern.test(lines[index])) {
      violations.push(`${relative(rootDir, file)}:${index + 1} -> 非工作流模块禁止直接依赖 Flowable: ${lines[index].trim()}`);
    }
  }
}

if (violations.length > 0) {
  console.error('Flowable 边界门禁失败。业务模块必须通过平台 WorkflowService 门面接入工作流：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('Flowable 边界门禁通过。');

/**
 * 递归列出 Java 源码文件。
 *
 * @param root 当前扫描路径，可以是文件或目录
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
