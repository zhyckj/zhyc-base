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
/** 工作区根目录，用于定位后台端和移动端 API 源码。 */
const workspaceRoot = resolve(serverRoot, '..');
/** 统一请求封装允许持有安全上下文参数和类型定义。 */
const allowedFiles = new Set([
  'zhyc-base-vue/src/api/http.ts',
  'zhyc-base-uniapp/src/api/request.ts',
]);
/** 需要扫描的前端 API 目录。 */
const apiSourceRoots = [
  resolve(workspaceRoot, 'zhyc-base-vue/src/api'),
  resolve(workspaceRoot, 'zhyc-base-uniapp/src/api'),
];
/** 业务 API 不应继续暴露的安全上下文参数名。 */
const securityContextParams = new Set([
  'tenantId',
  'currentUserId',
  'operatorUserId',
  'assigneeUserId',
  'starterUserId',
  'receiverId',
]);
/** 导出函数声明起始片段，用于静态扫描 API 函数签名。 */
const exportedFunctionPattern = /export\s+function\s+([A-Za-z0-9_]+)(?:<[^>{}]*>)?\s*\(/g;

const violations = [];

for (const apiSourceRoot of apiSourceRoots) {
  for (const file of listTypeScriptFiles(apiSourceRoot)) {
    const normalizedPath = relative(workspaceRoot, file);
    if (allowedFiles.has(normalizedPath)) {
      continue;
    }
    const source = readFileSync(file, 'utf8');
    collectUnusedSecurityContextParams(source, normalizedPath);
  }
}

if (violations.length > 0) {
  console.error('前端业务 API 函数签名禁止保留未使用的安全上下文参数，请改由统一 request 封装注入：');
  violations.forEach((violation) => console.error(`- ${violation}`));
  process.exit(1);
}

console.log('前端 API 签名门禁通过：业务 API 未保留未使用的安全上下文参数。');

/**
 * 收集导出函数中未参与业务请求的安全上下文参数。
 *
 * @param source TypeScript 源码
 * @param normalizedPath 相对工作区的文件路径
 */
function collectUnusedSecurityContextParams(source, normalizedPath) {
  exportedFunctionPattern.lastIndex = 0;
  let match;
  while ((match = exportedFunctionPattern.exec(source)) !== null) {
    const functionName = match[1];
    const paramsStart = exportedFunctionPattern.lastIndex;
    const paramsEnd = findMatchingCharacter(source, paramsStart - 1, '(', ')');
    if (paramsEnd < 0) {
      continue;
    }
    const bodyStart = source.indexOf('{', paramsEnd);
    if (bodyStart < 0) {
      continue;
    }
    const bodyEnd = findMatchingCharacter(source, bodyStart, '{', '}');
    if (bodyEnd < 0) {
      continue;
    }

    const paramsText = source.slice(paramsStart, paramsEnd);
    const bodyText = source.slice(bodyStart + 1, bodyEnd);
    for (const paramName of extractParameterNames(paramsText)) {
      if (!securityContextParams.has(paramName)) {
        continue;
      }
      if (!containsIdentifier(bodyText, paramName)) {
        const lineNumber = source.slice(0, match.index).split(/\r?\n/).length;
        violations.push(`${normalizedPath}:${lineNumber}: ${functionName}(${paramName})`);
      }
    }
    exportedFunctionPattern.lastIndex = bodyEnd + 1;
  }
}

/**
 * 从参数文本中提取参数名。
 *
 * @param paramsText 函数参数源码
 * @returns 参数名称列表
 */
function extractParameterNames(paramsText) {
  return splitTopLevel(paramsText, ',')
    .map((param) => param.trim())
    .filter(Boolean)
    .map((param) => param.match(/^([A-Za-z_$][A-Za-z0-9_$]*)\??\s*:/)?.[1])
    .filter(Boolean);
}

/**
 * 判断源码片段是否包含指定标识符。
 *
 * @param source 源码片段
 * @param identifier 标识符名称
 * @returns 是否命中完整标识符
 */
function containsIdentifier(source, identifier) {
  const pattern = new RegExp(`(^|[^A-Za-z0-9_$])${identifier}([^A-Za-z0-9_$]|$)`);
  return pattern.test(source);
}

/**
 * 按顶层分隔符拆分文本，忽略泛型、对象类型和函数类型内部的分隔符。
 *
 * @param source 源码文本
 * @param separator 分隔符
 * @returns 顶层拆分片段
 */
function splitTopLevel(source, separator) {
  const parts = [];
  let start = 0;
  let angleDepth = 0;
  let braceDepth = 0;
  let parenDepth = 0;
  for (let index = 0; index < source.length; index += 1) {
    const char = source[index];
    if (char === '<') {
      angleDepth += 1;
    } else if (char === '>') {
      angleDepth = Math.max(0, angleDepth - 1);
    } else if (char === '{') {
      braceDepth += 1;
    } else if (char === '}') {
      braceDepth = Math.max(0, braceDepth - 1);
    } else if (char === '(') {
      parenDepth += 1;
    } else if (char === ')') {
      parenDepth = Math.max(0, parenDepth - 1);
    } else if (char === separator && angleDepth === 0 && braceDepth === 0 && parenDepth === 0) {
      parts.push(source.slice(start, index));
      start = index + 1;
    }
  }
  parts.push(source.slice(start));
  return parts;
}

/**
 * 查找匹配的闭合字符位置。
 *
 * @param source 源码文本
 * @param openIndex 起始字符位置
 * @param openChar 起始字符
 * @param closeChar 闭合字符
 * @returns 匹配位置，不存在时返回 -1
 */
function findMatchingCharacter(source, openIndex, openChar, closeChar) {
  let depth = 0;
  for (let index = openIndex; index < source.length; index += 1) {
    const char = source[index];
    if (char === openChar) {
      depth += 1;
    } else if (char === closeChar) {
      depth -= 1;
      if (depth === 0) {
        return index;
      }
    }
  }
  return -1;
}

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
