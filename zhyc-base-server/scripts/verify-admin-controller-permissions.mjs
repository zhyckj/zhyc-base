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
const openApiControllerPattern = /@RequestMapping\s*\(\s*(?:value\s*=\s*|path\s*=\s*)?["']\/openapi\b/;
const excludedFiles = new Set([
  // 认证中心登录页重定向入口必须允许匿名访问，实际登录由 Spring Security 过滤器处理。
  'zhyc-auth-server/src/main/java/com/zhyc/auth/controller/AuthLoginPageController.java',
  // 移动端账号密码登录入口必须允许匿名访问，成功后才签发移动端令牌。
  'zhyc-auth-server/src/main/java/com/zhyc/auth/mobile/AuthMobileLoginController.java',
  'zhyc-module-purchase/src/main/java/com/zhyc/purchase/request/controller/PurRequestOpenApiController.java',
  // 当前用户权限接口要求已认证主体，不能再依赖某个业务权限，否则普通用户无法加载自己的权限清单。
  'zhyc-module-system/src/main/java/com/zhyc/system/permission/controller/SysCurrentPermissionController.java',
  // 后台 OAuth2 授权码换令牌 BFF 入口必须在登录前访问，鉴权由一次性授权码和服务端 client secret 共同完成。
  'zhyc-platform-app/src/main/java/com/zhyc/platform/auth/PlatformOAuthTokenController.java',
]);

/**
 * 递归列出 Java 控制器文件。
 *
 * @param dir 当前扫描目录
 * @returns 控制器源码文件路径
 */
function listControllerFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name === 'target' || path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listControllerFiles(path);
    }
    return entry.isFile() && entry.name.endsWith('Controller.java') ? [path] : [];
  });
}

/**
 * 判断 HTTP 映射方法是否缺少 Shiro 权限注解。
 *
 * @param lines 控制器源码行
 * @param lineIndex 当前映射注解行下标
 * @returns 缺少权限注解时返回 true
 */
function isMissingPermission(lines, lineIndex) {
  for (let index = lineIndex - 1; index >= 0; index -= 1) {
    const line = lines[index].trim();
    if (!line || line.startsWith('/**') || line.startsWith('*') || line.startsWith('*/')) {
      continue;
    }
    if (line.startsWith('@RequiresPermissions')) {
      return false;
    }
    if (line.startsWith('@')) {
      continue;
    }
    return true;
  }
  return true;
}

/**
 * 判断映射注解是否声明在方法上。
 *
 * @param lines 控制器源码行
 * @param lineIndex 当前映射注解行下标
 * @returns 方法级映射返回 true
 */
function isMethodMapping(lines, lineIndex) {
  for (let index = lineIndex + 1; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line || line.startsWith('/**') || line.startsWith('*') || line.startsWith('*/')) {
      continue;
    }
    if (line.startsWith('@')) {
      continue;
    }
    return !line.includes(' class ') && !line.startsWith('class ') && line.includes('(');
  }
  return false;
}

const controllerFiles = listControllerFiles(rootDir)
  .map((file) => ({ absolutePath: file, relativePath: relative(rootDir, file) }))
  .filter(({ absolutePath, relativePath }) => !excludedFiles.has(relativePath)
    && !isOpenApiController(absolutePath));

const violations = controllerFiles.flatMap(({ absolutePath, relativePath }) => {
  const lines = readFileSync(absolutePath, 'utf8').split(/\r?\n/);
  return lines.flatMap((line, index) => {
    if (!mappingPattern.test(line)
      || isPublicVisualMapping(line)
      || !isMethodMapping(lines, index)
      || !isMissingPermission(lines, index)) {
      return [];
    }
    return [`${relativePath}:${index + 1} -> ${line.trim()}`];
  });
});

/**
 * 判断控制器是否为开放 API 后端入口。
 *
 * @param absolutePath 控制器源码绝对路径
 * @returns 开放 API 控制器返回 true
 */
function isOpenApiController(absolutePath) {
  return openApiControllerPattern.test(readFileSync(absolutePath, 'utf8'));
}

/**
 * 判断是否为可视化公开发布访问接口。
 *
 * @param line 当前映射注解行
 * @returns 公开发布接口返回 true
 */
function isPublicVisualMapping(line) {
  return line.includes('"/public/') || line.includes("'/public/");
}

if (!existsSync(rootDir)) {
  console.error(`源码根目录不存在: ${rootDir}`);
  process.exit(1);
}

if (violations.length > 0) {
  console.error('后台 Controller 权限门禁失败，以下 HTTP 映射缺少 @RequiresPermissions：');
  violations.forEach((violation) => console.error(`- ${violation}`));
  process.exit(1);
}

console.log('后台 Controller 权限门禁通过。');
