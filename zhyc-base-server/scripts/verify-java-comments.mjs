/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const serverRoot = resolve(process.cwd());
const defaultScanRoots = [
  'zhyc-module-lowcode/src/main/java/com/zhyc/lowcode/generator',
  'zhyc-openapi-gateway/src/main/java/com/zhyc/openapi',
  'zhyc-module-workflow/src/main/java/com/zhyc/workflow',
];
const scanRoots = process.argv.slice(2).map((entry) => resolve(serverRoot, entry));
const targetRoots = scanRoots.length > 0 ? scanRoots : defaultScanRoots.map((entry) => resolve(serverRoot, entry));
const chinesePattern = /[\u3400-\u9fff]/;
const declarationPatterns = [
  {
    type: '类型',
    pattern: /^\s*(?:public|protected|private)?\s*(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+)?(?:class|interface|enum|record|@interface)\s+\w+/,
  },
  {
    type: '字段',
    pattern: /^\s*(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?[\w<>\[\],.? extends super]+\s+\w+\s*(?:=|;)/,
  },
  {
    type: '方法',
    pattern: /^\s*(?:public|protected)\s+(?:static\s+|final\s+|abstract\s+|synchronized\s+|default\s+|native\s+)*[\w<>\[\],.? extends super]+\s+\w+\s*\(/,
  },
  {
    type: '构造器',
    pattern: /^\s*(?:public|protected)\s+\w+\s*\(/,
  },
];

const violations = [];

for (const targetRoot of targetRoots) {
  if (!existsSync(targetRoot)) {
    violations.push(`${relative(serverRoot, targetRoot)} -> Java 注释扫描路径不存在`);
    continue;
  }
  for (const file of listJavaFiles(targetRoot)) {
    const lines = readFileSync(file, 'utf8').split(/\r?\n/);
    const documentedFields = collectDocumentedFields(lines);
    let insideTextBlock = false;
    for (let index = 0; index < lines.length; index += 1) {
      const line = lines[index];
      if (insideTextBlock) {
        if (countTextBlockDelimiters(line) % 2 === 1) {
          insideTextBlock = false;
        }
        continue;
      }
      if (countTextBlockDelimiters(line) % 2 === 1) {
        insideTextBlock = true;
        continue;
      }
      const declaration = resolveDeclarationType(line);
      if (!declaration) {
        continue;
      }
      if (declaration === '方法' && isDocumentedAccessor(line, documentedFields)) {
        continue;
      }
      const javaDoc = findPreviousJavaDoc(lines, index);
      if (!javaDoc) {
        violations.push(`${relative(serverRoot, file)}:${index + 1} -> ${declaration}缺少 Javadoc 中文注释: ${lines[index].trim()}`);
        continue;
      }
      if (!chinesePattern.test(javaDoc)) {
        violations.push(`${relative(serverRoot, file)}:${index + 1} -> ${declaration}Javadoc 缺少中文说明: ${lines[index].trim()}`);
      }
    }
  }
}

if (violations.length > 0) {
  console.error('Java 中文注释门禁失败。');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('Java 中文注释门禁通过。');

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

/**
 * 判断源码行是否为需要中文 Javadoc 的声明。
 *
 * @param line Java 源码行
 * @returns 声明类型，不需要检查时返回空字符串
 */
function resolveDeclarationType(line) {
  if (line.trim().startsWith('*') || line.trim().startsWith('//')) {
    return '';
  }
  const matchedDeclaration = declarationPatterns.find((candidate) => candidate.pattern.test(line));
  return matchedDeclaration?.type || '';
}

/**
 * 统计源码行中的 Java 文本块分隔符数量。
 *
 * @param line Java 源码行
 * @returns 当前行出现的文本块分隔符数量
 */
function countTextBlockDelimiters(line) {
  return line.split('"""').length - 1;
}

/**
 * 收集已有中文 Javadoc 的字段名称。
 *
 * @param lines Java 源码行
 * @returns 已由中文字段注释覆盖语义的字段名称集合
 */
function collectDocumentedFields(lines) {
  const documentedFields = new Set();
  for (let index = 0; index < lines.length; index += 1) {
    if (!declarationPatterns[1].pattern.test(lines[index])) {
      continue;
    }
    const javaDoc = findPreviousJavaDoc(lines, index);
    if (!javaDoc || !chinesePattern.test(javaDoc)) {
      continue;
    }
    const fieldMatch = lines[index].trim().match(/\s+(\w+)\s*(?:=|;)/);
    if (fieldMatch) {
      documentedFields.add(fieldMatch[1]);
    }
  }
  return documentedFields;
}

/**
 * 判断方法是否为可复用字段注释语义的 JavaBean 访问器。
 *
 * @param line Java 方法声明行
 * @param documentedFields 已由中文字段注释覆盖语义的字段名称集合
 * @returns 可由字段注释覆盖时返回 true
 */
function isDocumentedAccessor(line, documentedFields) {
  const methodMatch = line.trim().match(/\s(\w+)\s*\(/);
  if (!methodMatch) {
    return false;
  }
  const methodName = methodMatch[1];
  const propertyName = resolveAccessorPropertyName(methodName);
  return propertyName ? documentedFields.has(propertyName) : false;
}

/**
 * 解析 JavaBean 访问器对应的属性名称。
 *
 * @param methodName 方法名称
 * @returns 属性名称，不是访问器时返回空字符串
 */
function resolveAccessorPropertyName(methodName) {
  if (methodName.startsWith('get') && methodName.length > 3) {
    return decapitalize(methodName.slice(3));
  }
  if (methodName.startsWith('set') && methodName.length > 3) {
    return decapitalize(methodName.slice(3));
  }
  if (methodName.startsWith('is') && methodName.length > 2) {
    return decapitalize(methodName.slice(2));
  }
  return '';
}

/**
 * 将 JavaBean 属性首字母转为小写。
 *
 * @param value 属性名称片段
 * @returns 首字母小写后的属性名称
 */
function decapitalize(value) {
  if (!value) {
    return value;
  }
  return value.charAt(0).toLowerCase() + value.slice(1);
}

/**
 * 查找声明上方最近的 Javadoc 注释块。
 *
 * @param lines Java 源码行
 * @param declarationIndex 声明所在行下标
 * @returns Javadoc 注释文本，缺失时返回空字符串
 */
function findPreviousJavaDoc(lines, declarationIndex) {
  let index = declarationIndex - 1;
  while (index >= 0) {
    const line = lines[index].trim();
    if (!line || line.startsWith('@')) {
      index -= 1;
      continue;
    }
    const annotationStartIndex = findAnnotationStartIndex(lines, index);
    if (annotationStartIndex >= 0) {
      index = annotationStartIndex - 1;
      continue;
    }
    if (line.endsWith('*/')) {
      const javaDocLines = [line];
      index -= 1;
      while (index >= 0) {
        javaDocLines.unshift(lines[index].trim());
        if (lines[index].trim().startsWith('/**')) {
          return javaDocLines.join('\n');
        }
        index -= 1;
      }
      return '';
    }
    return '';
  }
  return '';
}

/**
 * 从注解多行参数中回溯注解起始行。
 *
 * @param lines Java 源码行
 * @param currentIndex 当前源码行下标
 * @returns 注解起始行下标，不属于注解参数时返回 -1
 */
function findAnnotationStartIndex(lines, currentIndex) {
  let parenthesisBalance = 0;
  for (let index = currentIndex; index >= 0; index -= 1) {
    const line = lines[index].trim();
    if (!line) {
      return -1;
    }
    parenthesisBalance += countChar(line, ')') - countChar(line, '(');
    if (line.startsWith('@')) {
      return parenthesisBalance >= 0 ? index : -1;
    }
    if (line.endsWith(';') || line.endsWith('{') || line.endsWith('}')) {
      return -1;
    }
  }
  return -1;
}

/**
 * 统计指定字符出现次数。
 *
 * @param value 原始文本
 * @param char 目标字符
 * @returns 出现次数
 */
function countChar(value, char) {
  return value.split(char).length - 1;
}
