/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync, readdirSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const rootDir = resolve(process.cwd());
const excludedVendorDdlFiles = new Set([
  'zhyc-module-workflow/src/main/resources/db/V2__flowable_engine_mysql.sql',
]);
const ddlFiles = listSqlFiles(rootDir).filter((file) => file.includes('/src/main/resources/db/')
  && !excludedVendorDdlFiles.has(relative(rootDir, file)));
const constraintPrefixes = [
  'PRIMARY ',
  'UNIQUE ',
  'KEY ',
  'INDEX ',
  'CONSTRAINT ',
  'FOREIGN ',
  'FULLTEXT ',
  'SPATIAL ',
];

/**
 * 递归列出 SQL 文件。
 *
 * @param dir 当前扫描目录
 * @returns SQL 文件路径列表
 */
function listSqlFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      return entry.name === 'target' ? [] : listSqlFiles(path);
    }
    return entry.isFile() && entry.name.endsWith('.sql') ? [path] : [];
  });
}

/**
 * 判断当前行是否是表字段定义。
 *
 * @param line SQL 行
 * @returns 字段定义返回 true
 */
function isColumnDefinition(line) {
  const normalized = line.trim();
  if (!normalized || normalized.startsWith('--') || normalized.startsWith(')')) {
    return false;
  }
  return !constraintPrefixes.some((prefix) => normalized.toUpperCase().startsWith(prefix));
}

const violations = [];

for (const file of ddlFiles) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  let insideTable = false;
  let currentTable = '';
  let insideConstraint = false;
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index];
    const trimmedLine = line.trim();
    const createTableMatch = trimmedLine.match(/^CREATE TABLE(?: IF NOT EXISTS)?\s+`?([a-zA-Z0-9_]+)`?/i);
    if (createTableMatch) {
      insideTable = true;
      currentTable = createTableMatch[1];
      continue;
    }
    if (!insideTable) {
      continue;
    }
    if (insideConstraint) {
      if (trimmedLine.startsWith(')') && trimmedLine.endsWith(',')) {
        insideConstraint = false;
        continue;
      }
      if (!trimmedLine.startsWith(')')) {
        insideConstraint = !trimmedLine.endsWith(',');
        continue;
      }
      insideConstraint = false;
    }
    if (trimmedLine.startsWith(')')) {
      if (!/\bCOMMENT\s*=/i.test(trimmedLine)) {
        violations.push(`${relative(rootDir, file)}:${index + 1} -> 表 ${currentTable} 缺少表注释`);
      }
      insideTable = false;
      currentTable = '';
      continue;
    }
    if (constraintPrefixes.some((prefix) => trimmedLine.toUpperCase().startsWith(prefix))) {
      insideConstraint = trimmedLine.toUpperCase().startsWith('CONSTRAINT ')
          || (!trimmedLine.includes(')') && !trimmedLine.endsWith(','));
      continue;
    }
    if (isColumnDefinition(trimmedLine) && !/\bCOMMENT\b/i.test(trimmedLine)) {
      violations.push(`${relative(rootDir, file)}:${index + 1} -> 字段缺少 COMMENT: ${trimmedLine}`);
    }
  }
}

if (violations.length > 0) {
  console.error('数据库 DDL 注释门禁失败。');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('数据库 DDL 注释门禁通过。');
