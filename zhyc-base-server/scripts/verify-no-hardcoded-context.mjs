/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { extname, join, relative, resolve } from 'node:path';

const workspaceRoot = resolve(process.argv[2] || resolve(process.cwd(), '..'));
const scanRoots = [
  resolve(workspaceRoot, 'zhyc-base-server'),
  resolve(workspaceRoot, 'zhyc-base-vue/src'),
  resolve(workspaceRoot, 'zhyc-base-uniapp/src'),
].filter(existsSync);
const allowedExtensions = new Set(['.java', '.ts', '.tsx', '.vue', '.sql']);
const forbiddenPatterns = [
  { pattern: /['"]tenant_a['"]/, label: '硬编码演示租户 tenant_a' },
  { pattern: /\b(?:currentUserId|operatorUserId|assigneeUserId|starterUserId|receiverId|userId)\s*[:=]\s*\d+\b/, label: '硬编码用户上下文' },
  { pattern: /\b(?:orgId|deptId|departmentId)\s*[:=]\s*\d+\b/, label: '硬编码组织上下文' },
];
const violations = [];

for (const root of scanRoots) {
  for (const file of listSourceFiles(root)) {
    const lines = readFileSync(file, 'utf8').split(/\r?\n/);
    lines.forEach((line, index) => {
      if (isAllowedLine(line)) {
        return;
      }
      for (const { pattern, label } of forbiddenPatterns) {
        if (pattern.test(line)) {
          violations.push(`${relative(workspaceRoot, file)}:${index + 1} -> ${label}: ${line.trim()}`);
        }
      }
    });
  }
}

if (violations.length > 0) {
  console.error('硬编码上下文门禁失败。租户和用户上下文必须通过统一上下文工具或服务端认证上下文获取：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('硬编码上下文门禁通过。');

/**
 * 递归列出生产源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 生产源码文件路径
 */
function listSourceFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isScannableFile(dir) ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'dist', 'node_modules', '.git', 'scripts', 'docs'].includes(entry.name)) {
        return [];
      }
      if (path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listSourceFiles(path);
    }
    return entry.isFile() && isScannableFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否属于硬编码上下文扫描范围。
 *
 * @param file 文件路径
 * @returns 需要扫描返回 true
 */
function isScannableFile(file) {
  const normalizedPath = file.split('\\').join('/');
  if (normalizedPath.includes('/src/test/') || normalizedPath.includes('/target/')) {
    return false;
  }
  return allowedExtensions.has(extname(file));
}

/**
 * 判断源码行是否属于允许出现固定上下文值的说明性内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}
