/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync, readdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { extname, join, relative, resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(serverRoot, '..');
const scanRoots = [
  serverRoot,
  resolve(workspaceRoot, 'zhyc-base-vue/src'),
  resolve(workspaceRoot, 'zhyc-base-uniapp/src'),
];
const allowedExtensions = new Set(['.java', '.ts', '.tsx', '.vue', '.sql', '.yml', '.yaml', '.properties']);
const forbiddenPatterns = [
  { pattern: /(\/\/|\/\*|\*)[^\n]*\bTODO\b/, label: 'TODO' },
  { pattern: /(\/\/|\/\*|\*)[^\n]*\bFIXME\b/, label: 'FIXME' },
  { pattern: /待接入|接口暂未接入|后续迭代|临时占位|占位实现|dummy|stub/i, label: '未完成占位' },
];

/**
 * 递归列出生产源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 生产源码文件路径
 */
function listSourceFiles(dir) {
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
    return entry.isFile() && allowedExtensions.has(extname(entry.name)) ? [path] : [];
  });
}

const violations = [];

for (const root of scanRoots) {
  for (const file of listSourceFiles(root)) {
    const lines = readFileSync(file, 'utf8').split(/\r?\n/);
    lines.forEach((line, index) => {
      forbiddenPatterns.forEach(({ pattern, label }) => {
        if (pattern.test(line)) {
          violations.push(`${relative(workspaceRoot, file)}:${index + 1} -> ${label}: ${line.trim()}`);
        }
      });
    });
  }
}

if (violations.length > 0) {
  console.error('生产源码占位门禁失败。');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('生产源码占位门禁通过。');
