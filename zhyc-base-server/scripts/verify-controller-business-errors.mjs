/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const serverRoot = resolve(process.argv[2] || process.cwd());
const sourceRoot = resolve(serverRoot, 'zhyc-base-server');
const scanRoot = existsSync(sourceRoot) ? sourceRoot : serverRoot;
const violations = [];

for (const file of listControllerFiles(scanRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (isAllowedLine(line)) {
      return;
    }
    if (line.includes('throw new IllegalArgumentException(')) {
      violations.push(`${relative(serverRoot, file)}:${index + 1} -> Controller 不得直接抛裸参数异常: ${line.trim()}`);
    }
  });
}

if (violations.length > 0) {
  console.error('Controller 业务异常门禁失败。生产 Controller 的业务参数错误必须使用带稳定错误码的 BusinessException：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('Controller 业务异常门禁通过。');

/**
 * 递归列出生产 Controller 源码文件。
 *
 * @param dir 当前扫描目录
 * @returns Controller 源码文件路径
 */
function listControllerFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isControllerFile(dir) ? [dir] : [];
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
      return listControllerFiles(path);
    }
    return entry.isFile() && isControllerFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否为生产 Controller Java 文件。
 *
 * @param file 文件路径
 * @returns 属于扫描范围时返回 true
 */
function isControllerFile(file) {
  const normalizedPath = file.split('\\').join('/');
  return normalizedPath.endsWith('Controller.java')
    && normalizedPath.includes('/src/main/java/')
    && !normalizedPath.includes('/src/test/')
    && !normalizedPath.includes('/target/');
}

/**
 * 判断源码行是否属于允许跳过的说明性内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}
