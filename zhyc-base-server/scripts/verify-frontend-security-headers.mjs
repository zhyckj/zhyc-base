/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

/** 当前脚本所在后端工程根目录。 */
const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
/** 工作区根目录，用于定位后台端和移动端工程。 */
const workspaceRoot = resolve(serverRoot, '..');
/** 允许集中注入安全请求头的统一封装文件。 */
const allowedHeaderFiles = new Set([
  'zhyc-base-vue/src/api/http.ts',
  'zhyc-base-uniapp/src/api/request.ts',
]);
/** 安全上下文请求头只能由统一封装从登录态生成，业务 API 不得手工传入。 */
const forbiddenHeaderPatterns = [
  /\bAuthorization\b/,
  /X-ZHYC-Tenant-Id/,
  /X-ZHYC-User-Id/,
  /X-ZHYC-Access-Key/,
  /X-ZHYC-Signature/,
];
/** 需要扫描的前端 API 源码目录。 */
const apiSourceRoots = [
  resolve(workspaceRoot, 'zhyc-base-vue/src/api'),
  resolve(workspaceRoot, 'zhyc-base-uniapp/src/api'),
];

const violations = [];

for (const apiSourceRoot of apiSourceRoots) {
  for (const file of listTypeScriptFiles(apiSourceRoot)) {
    const normalizedPath = relative(workspaceRoot, file);
    if (allowedHeaderFiles.has(normalizedPath)) {
      continue;
    }
    const lines = readFileSync(file, 'utf8').split(/\r?\n/);
    lines.forEach((line, index) => {
      if (forbiddenHeaderPatterns.some((pattern) => pattern.test(line))) {
        violations.push(`${normalizedPath}:${index + 1}: ${line.trim()}`);
      }
    });
  }
}

if (violations.length > 0) {
  console.error('前端业务 API 禁止手工声明安全上下文请求头，请统一通过 request 封装注入：');
  violations.forEach((violation) => console.error(`- ${violation}`));
  process.exit(1);
}

console.log('前端安全请求头门禁通过：业务 API 未手工声明租户、用户、鉴权或签名请求头。');

/**
 * 递归列出 TypeScript API 源码文件。
 *
 * @param root API 源码目录
 * @returns TypeScript 文件绝对路径列表
 */
function listTypeScriptFiles(root) {
  if (!existsSync(root)) {
    return [];
  }
  const files = [];
  for (const entry of readdirSync(root)) {
    const absolutePath = resolve(root, entry);
    const stat = statSync(absolutePath);
    if (stat.isDirectory()) {
      files.push(...listTypeScriptFiles(absolutePath));
      continue;
    }
    if (absolutePath.endsWith('.ts')) {
      files.push(absolutePath);
    }
  }
  return files;
}
