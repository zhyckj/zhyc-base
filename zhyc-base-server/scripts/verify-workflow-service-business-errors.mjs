/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const root = resolve(process.argv[2] || process.cwd());
const scanRoot = resolveWorkflowSourceRoot(root);
const violations = [];

for (const file of listWorkflowServiceFiles(scanRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (isAllowedLine(line)) {
      return;
    }
    const trimmed = line.trim();
    if (trimmed.includes('throw new IllegalArgumentException(')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 工作流服务层不得直接抛裸参数异常: ${trimmed}`);
    }
    if (trimmed.includes('Objects.requireNonNull(') && !trimmed.startsWith('this.')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 工作流服务公开参数不得使用 Objects.requireNonNull 暴露 NPE: ${trimmed}`);
    }
  });
}

if (violations.length > 0) {
  console.error('工作流服务业务异常门禁失败。服务层面向调用方的参数或业务错误必须使用带稳定错误码的 BusinessException：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('工作流服务业务异常门禁通过。');

/**
 * 解析工作流模块生产源码根目录。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @returns 工作流模块生产源码根目录
 */
function resolveWorkflowSourceRoot(startRoot) {
  const candidates = [
    resolve(startRoot, 'zhyc-module-workflow/src/main/java/com/zhyc/workflow'),
    resolve(startRoot, 'zhyc-base-server/zhyc-module-workflow/src/main/java/com/zhyc/workflow'),
  ];
  const matchedRoot = candidates.find((candidate) => existsSync(candidate));
  if (matchedRoot) {
    return matchedRoot;
  }
  console.error(`工作流服务业务异常门禁失败。未找到工作流模块生产源码目录：${candidates.join(' 或 ')}`);
  process.exit(1);
}

/**
 * 递归列出工作流生产服务源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 服务源码文件路径
 */
function listWorkflowServiceFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isWorkflowServiceFile(dir) ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'node_modules', '.git'].includes(entry.name)) {
        return [];
      }
      if (path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listWorkflowServiceFiles(path);
    }
    return entry.isFile() && isWorkflowServiceFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否为工作流服务源码文件。
 *
 * @param file 文件路径
 * @returns 属于扫描范围时返回 true
 */
function isWorkflowServiceFile(file) {
  const normalizedPath = file.split('\\').join('/');
  return normalizedPath.endsWith('Service.java')
    && normalizedPath.includes('/src/main/java/com/zhyc/workflow/')
    && !normalizedPath.includes('/repository/')
    && !normalizedPath.includes('/src/test/')
    && !normalizedPath.includes('/target/');
}

/**
 * 判断源码行是否属于允许跳过的注释内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}
