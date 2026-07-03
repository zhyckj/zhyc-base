/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const rootDir = resolve(process.argv[2] || process.cwd());
const mappingPattern = /@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping)\b/;
const exceptionHandlerPattern = /@ExceptionHandler\b/;
const violations = [];

if (!existsSync(rootDir)) {
  console.error(`源码根目录不存在: ${rootDir}`);
  process.exit(1);
}

for (const controllerFile of listControllerFiles(rootDir)) {
  checkControllerFile(controllerFile);
}
for (const javaFile of listJavaFiles(rootDir)) {
  checkExceptionHandlerFile(javaFile);
}

if (violations.length > 0) {
  console.error('Controller 统一响应门禁失败，以下 HTTP 接口或异常处理方法未返回 ApiResult 统一响应结构：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('Controller 统一响应门禁通过。');

/**
 * 递归列出 Java 源码文件。
 *
 * @param dir 当前扫描目录
 * @returns Java 源码文件路径列表
 */
function listJavaFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', '.git'].includes(entry.name)) {
        return [];
      }
      return listJavaFiles(path);
    }
    return entry.isFile() && entry.name.endsWith('.java') ? [path] : [];
  });
}

/**
 * 递归列出 Java Controller 源码文件。
 *
 * @param dir 当前扫描目录
 * @returns Controller 文件路径列表
 */
function listControllerFiles(dir) {
  return listJavaFiles(dir).filter((file) => file.endsWith('Controller.java'));
}

/**
 * 检查单个 Controller 文件中的 HTTP 方法返回类型。
 *
 * @param controllerFile Controller 文件路径
 */
function checkControllerFile(controllerFile) {
  const relativePath = relative(rootDir, controllerFile);
  if (isLoginPageController(relativePath)) {
    return;
  }
  const lines = readFileSync(controllerFile, 'utf8').split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    if (!mappingPattern.test(lines[index]) || !isMethodMapping(lines, index)) {
      continue;
    }
    const signature = collectMethodSignature(lines, index);
    const method = readPublicMethod(signature);
    if (!method || isApiResultType(method.returnType)) {
      continue;
    }
    violations.push(`${relativePath}:${index + 1} -> ${method.signature}`);
  }
}

/**
 * 检查异常处理方法是否返回统一响应。
 *
 * @param javaFile Java 源码文件路径
 */
function checkExceptionHandlerFile(javaFile) {
  const relativePath = relative(rootDir, javaFile);
  const lines = readFileSync(javaFile, 'utf8').split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    if (!exceptionHandlerPattern.test(lines[index])) {
      continue;
    }
    const signature = collectMethodSignature(lines, index);
    const method = readPublicMethod(signature);
    if (!method || isApiResultType(method.returnType)) {
      continue;
    }
    violations.push(`${relativePath}:${index + 1} -> ${method.signature}`);
  }
}

/**
 * 判断映射注解是否声明在方法上。
 *
 * @param lines Controller 源码行
 * @param lineIndex 映射注解行下标
 * @returns 方法级映射返回 true
 */
function isMethodMapping(lines, lineIndex) {
  for (let index = lineIndex + 1; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line || isCommentLine(line) || line.startsWith('@')) {
      continue;
    }
    return !line.includes(' class ') && !line.startsWith('class ') && line.includes('(');
  }
  return false;
}

/**
 * 从映射注解后收集方法签名。
 *
 * @param lines Controller 源码行
 * @param mappingLineIndex 映射注解行下标
 * @returns 单行方法签名
 */
function collectMethodSignature(lines, mappingLineIndex) {
  const signatureLines = [];
  for (let index = mappingLineIndex + 1; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line || isCommentLine(line) || line.startsWith('@')) {
      continue;
    }
    signatureLines.push(line);
    if (line.includes('{') || line.includes(';')) {
      break;
    }
  }
  return signatureLines.join(' ').replace(/\s+/g, ' ').trim();
}

/**
 * 读取 public 方法的返回类型和签名。
 *
 * @param signature 单行方法签名
 * @returns 方法信息，无法解析时返回 undefined
 */
function readPublicMethod(signature) {
  const normalizedSignature = signature.replace(/\s*\{\s*$/, '').trim();
  const match = normalizedSignature.match(/^public\s+(?:static\s+|final\s+|synchronized\s+)*(.+?)\s+([A-Za-z_]\w*)\s*\(/);
  if (!match) {
    return undefined;
  }
  return {
    returnType: match[1].replace(/^<[^>]+>\s*/, '').trim(),
    signature: normalizedSignature,
  };
}

/**
 * 判断返回类型是否为统一响应结构。
 *
 * @param returnType 方法返回类型
 * @returns 是 ApiResult 返回 true
 */
function isApiResultType(returnType) {
  const normalizedReturnType = returnType.replace(/\s+/g, '');
  return normalizedReturnType === 'ApiResult'
      || normalizedReturnType.startsWith('ApiResult<')
      || normalizedReturnType === 'AuthMobileApiResult'
      || normalizedReturnType.startsWith('AuthMobileApiResult<')
      || normalizedReturnType === 'com.zhyc.common.api.ApiResult'
      || normalizedReturnType.startsWith('com.zhyc.common.api.ApiResult<');
}

/**
 * 判断是否为认证中心登录页重定向控制器。
 *
 * @param relativePath Controller 相对路径
 * @returns 登录页控制器返回 true
 */
function isLoginPageController(relativePath) {
  return relativePath === 'zhyc-auth-server/src/main/java/com/zhyc/auth/controller/AuthLoginPageController.java';
}

/**
 * 判断源码行是否为注释行。
 *
 * @param line 已裁剪空白的源码行
 * @returns 注释行返回 true
 */
function isCommentLine(line) {
  return line.startsWith('/**')
      || line.startsWith('*')
      || line.startsWith('*/')
      || line.startsWith('//');
}
