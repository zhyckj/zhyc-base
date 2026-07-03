/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const backendRoot = process.env.ZHYC_BACKEND_PERMISSION_FIXTURE_ROOT
  ? resolve(process.env.ZHYC_BACKEND_PERMISSION_FIXTURE_ROOT)
  : serverRoot;

const declaredPermissions = collectModulePermissions(backendRoot);
const annotationPermissions = collectAnnotationPermissions(backendRoot);
const missingPermissions = [...annotationPermissions]
  .filter((permission) => !declaredPermissions.has(permission))
  .sort();

if (missingPermissions.length > 0) {
  console.error('后端权限元数据校验失败，以下 @RequiresPermissions 未在模块 META-INF/zhyc-module.yml 中声明：');
  for (const permission of missingPermissions) {
    console.error(`- ${permission}`);
  }
  process.exit(1);
}

console.log('后端权限元数据校验通过。');

/**
 * 收集生产源码中的 Shiro 权限注解编码。
 *
 * <p>低代码内置模板中的 `{module}:{entity}` 是生成期占位符，不属于平台运行时权限，需排除。</p>
 *
 * @param root 后端工程根目录
 * @returns 注解权限编码集合
 */
function collectAnnotationPermissions(root) {
  const permissions = new Set();
  for (const file of findJavaMainFiles(root)) {
    const source = readFileSync(file, 'utf8');
    const pattern = /@RequiresPermissions\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']/g;
    for (const match of source.matchAll(pattern)) {
      const permission = match[1];
      if (!permission.includes('{') && !permission.includes('}')) {
        permissions.add(permission);
      }
    }
  }
  return permissions;
}

/**
 * 收集模块元数据声明的权限编码。
 *
 * @param root 后端工程根目录
 * @returns 模块权限编码集合
 */
function collectModulePermissions(root) {
  const permissions = new Set();
  for (const file of findModuleYamlFiles(root)) {
    for (const permission of parsePermissionsSection(readFileSync(file, 'utf8'))) {
      permissions.add(permission);
    }
  }
  return permissions;
}

/**
 * 查找生产 Java 源码文件。
 *
 * @param root 后端工程根目录
 * @returns Java 源码路径列表
 */
function findJavaMainFiles(root) {
  if (!existsSync(root)) {
    throw new Error(`后端工程目录不存在: ${root}`);
  }
  const matchedFiles = [];
  walk(root, (file) => {
    if (file.includes('/src/main/java/') && file.endsWith('.java')) {
      matchedFiles.push(file);
    }
  });
  return matchedFiles;
}

/**
 * 查找模块描述文件。
 *
 * @param root 后端工程根目录
 * @returns 模块描述文件路径列表
 */
function findModuleYamlFiles(root) {
  const matchedFiles = [];
  walk(root, (file) => {
    if (file.endsWith('/src/main/resources/META-INF/zhyc-module.yml')) {
      matchedFiles.push(file);
    }
  });
  return matchedFiles;
}

/**
 * 递归遍历目录。
 *
 * @param dir 当前目录
 * @param visitor 文件访问回调
 */
function walk(dir, visitor) {
  for (const entry of readdirSync(dir)) {
    if (entry === 'target' || entry === 'node_modules' || entry === '.git') {
      continue;
    }
    const path = resolve(dir, entry);
    const stat = statSync(path);
    if (stat.isDirectory()) {
      walk(path, visitor);
      continue;
    }
    visitor(path);
  }
}

/**
 * 解析 YAML 中的 permissions 列表。
 *
 * @param source YAML 源码
 * @returns 权限编码列表
 */
function parsePermissionsSection(source) {
  const permissions = [];
  let inPermissions = false;
  for (const line of source.split(/\r?\n/)) {
    if (/^\S[^:]*:\s*$/.test(line)) {
      inPermissions = line.trim() === 'permissions:';
      continue;
    }
    if (!inPermissions) {
      continue;
    }
    const itemMatch = line.match(/^\s*-\s+([A-Za-z0-9:_-]+)\s*$/);
    if (itemMatch) {
      permissions.push(itemMatch[1]);
    }
  }
  return permissions;
}
