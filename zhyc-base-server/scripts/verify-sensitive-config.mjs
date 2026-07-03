/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync, readdirSync } from 'node:fs';
import { basename, extname, join, relative, resolve } from 'node:path';

const workspaceRoot = resolve(process.cwd(), '..');
const sensitiveKeyPattern = /(password|passwd|pwd|secret|token|private-key|private_key|access-key|access_key|api-key|api_key|client-secret|client_secret)/i;
const configFilePattern = /^(application.*\.(yml|yaml|properties)|\.env(\..*)?)$/;

/**
 * 递归列出配置文件。
 *
 * @param dir 当前扫描目录
 * @returns 配置文件路径
 */
function listConfigFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'dist', 'node_modules', '.git'].includes(entry.name)) {
        return [];
      }
      return listConfigFiles(path);
    }
    return entry.isFile() && configFilePattern.test(entry.name) ? [path] : [];
  });
}

/**
 * 判断配置值是否使用环境变量占位。
 *
 * @param value 配置值
 * @returns 使用环境变量占位时返回 true
 */
function isEnvPlaceholder(value) {
  const normalized = value.trim().replace(/^['"]|['"]$/g, '');
  return normalized === '' || normalized.startsWith('${');
}

/**
 * 解析配置行中的键值对。
 *
 * @param line 配置文件行
 * @returns 键值对，非配置行返回 null
 */
function parseConfigEntry(line) {
  const trimmedLine = line.trim();
  if (!trimmedLine || trimmedLine.startsWith('#') || trimmedLine.startsWith('//')) {
    return null;
  }
  const yamlMatch = trimmedLine.match(/^([A-Za-z0-9_.-]+)\s*:\s*(.*)$/);
  if (yamlMatch) {
    return { key: yamlMatch[1], value: yamlMatch[2] };
  }
  const propertyMatch = trimmedLine.match(/^([A-Za-z0-9_.-]+)\s*=\s*(.*)$/);
  if (propertyMatch) {
    return { key: propertyMatch[1], value: propertyMatch[2] };
  }
  return null;
}

const violations = [];

for (const file of listConfigFiles(workspaceRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    const entry = parseConfigEntry(line);
    if (!entry || !sensitiveKeyPattern.test(entry.key)) {
      return;
    }
    if (!isEnvPlaceholder(entry.value)) {
      violations.push(`${relative(workspaceRoot, file)}:${index + 1} -> ${entry.key} 必须使用环境变量占位`);
    }
  });
}

if (violations.length > 0) {
  console.error('敏感配置明文门禁失败。');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('敏感配置明文门禁通过。');
