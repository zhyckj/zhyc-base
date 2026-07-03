/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || process.cwd());
const violations = [];

if (!existsSync(rootDir)) {
  console.error(`扫描根目录不存在: ${rootDir}`);
  process.exit(1);
}

for (const file of listSourceFiles(rootDir)) {
  const relativePath = relative(rootDir, file);
  const content = readFileSync(file, 'utf8');
  for (const statement of extractSqlStatements(content)) {
    const normalizedSql = normalizeSql(statement.sql);
    if (/\bselect\s+\*/i.test(normalizedSql)) {
      violations.push(`${relativePath}:${statement.lineNumber} -> 禁止生产 SELECT *: ${statement.preview}`);
    }
    if (/^\s*update\b/i.test(normalizedSql) && !/\bwhere\b/i.test(normalizedSql)) {
      violations.push(`${relativePath}:${statement.lineNumber} -> 禁止无条件 UPDATE: ${statement.preview}`);
    }
    if (/^\s*delete\s+from\b/i.test(normalizedSql) && !/\bwhere\b/i.test(normalizedSql)) {
      violations.push(`${relativePath}:${statement.lineNumber} -> 禁止无条件 DELETE: ${statement.preview}`);
    }
  }
}

if (violations.length > 0) {
  console.error('SQL 安全门禁失败。');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('SQL 安全门禁通过。');

/**
 * 递归列出需要扫描的生产 SQL 与 Java 文件。
 *
 * @param root 当前扫描路径
 * @returns 文件路径列表
 */
function listSourceFiles(root) {
  const rootStat = statSync(root);
  if (rootStat.isFile()) {
    return isScannableFile(root) ? [root] : [];
  }
  return readdirSync(root, { withFileTypes: true }).flatMap((entry) => {
    const path = join(root, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'node_modules', 'dist', '.git'].includes(entry.name)) {
        return [];
      }
      return listSourceFiles(path);
    }
    return entry.isFile() && isScannableFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否属于生产 SQL 安全扫描范围。
 *
 * @param file 文件路径
 * @returns 需要扫描返回 true
 */
function isScannableFile(file) {
  const normalizedPath = file.split('\\').join('/');
  if (normalizedPath.includes('/src/test/') || normalizedPath.includes('/target/')) {
    return false;
  }
  if (normalizedPath.endsWith('.sql')) {
    return normalizedPath.includes('/src/main/resources/db/');
  }
  return normalizedPath.endsWith('.java') && normalizedPath.includes('/src/main/java/');
}

/**
 * 从源码中提取疑似 SQL 语句。
 *
 * @param content 文件内容
 * @returns SQL 语句及起始行号
 */
function extractSqlStatements(content) {
  const statements = [];
  const lines = content.split(/\r?\n/);
  let currentSql = '';
  let currentLineNumber = 0;
  for (let index = 0; index < lines.length; index += 1) {
    const candidate = sanitizeSourceLine(lines[index]);
    if (!currentSql && !/^\s*(select|update|delete\s+from)\b/i.test(candidate)) {
      continue;
    }
    if (!currentSql) {
      currentLineNumber = index + 1;
    }
    currentSql = `${currentSql} ${candidate}`.trim();
    if (candidate.includes(';') || candidate.includes('"""') || !isLikelyMultilineSql(currentSql)) {
      statements.push({
        lineNumber: currentLineNumber,
        sql: currentSql,
        preview: currentSql.replace(/\s+/g, ' ').slice(0, 160),
      });
      currentSql = '';
      currentLineNumber = 0;
    }
  }
  if (currentSql) {
    statements.push({
      lineNumber: currentLineNumber,
      sql: currentSql,
      preview: currentSql.replace(/\s+/g, ' ').slice(0, 160),
    });
  }
  return statements;
}

/**
 * 清理源码行中的字符串语法和 SQL 行注释。
 *
 * @param line 源码行
 * @returns 便于匹配的 SQL 文本
 */
function sanitizeSourceLine(line) {
  return line
      .replace(/--.*$/, '')
      .replace(/^\s*\+\s*/, '')
      .replace(/^['"`]+/, '')
      .replace(/['"`]+\s*$/, '')
      .trim();
}

/**
 * 判断当前 SQL 片段是否可能还未结束。
 *
 * @param sql SQL 片段
 * @returns 可能跨行时返回 true
 */
function isLikelyMultilineSql(sql) {
  return /\b(select|update|delete\s+from)\b/i.test(sql)
      && !/\bwhere\b/i.test(sql)
      && !/\bvalues\b/i.test(sql);
}

/**
 * 标准化 SQL 文本。
 *
 * @param sql SQL 文本
 * @returns 标准化后的 SQL
 */
function normalizeSql(sql) {
  return sql
      .replace(/"""/g, '')
      .replace(/\s+/g, ' ')
      .trim();
}
