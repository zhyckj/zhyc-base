/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve, relative } from 'node:path';

const serverRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = process.env.ZHYC_FRONTEND_PERMISSION_FIXTURE_ROOT
  ? resolve(process.env.ZHYC_FRONTEND_PERMISSION_FIXTURE_ROOT)
  : resolve(serverRoot, '..');
const routeFile = resolve(workspaceRoot, 'zhyc-base-vue/src/router/routes.ts');
const backendRoot = resolve(workspaceRoot, 'zhyc-base-server');

const routePermissions = collectRoutePermissions(routeFile);
const modulePermissions = collectModulePermissions(backendRoot);
const missingPermissions = [...routePermissions]
  .filter((permission) => !modulePermissions.has(permission))
  .sort();

if (missingPermissions.length > 0) {
  console.error('前端路由权限声明校验失败，以下路由权限未在后端模块 META-INF/zhyc-module.yml 中声明：');
  for (const permission of missingPermissions) {
    console.error(`- ${permission}`);
  }
  process.exit(1);
}

console.log('前端路由权限声明校验通过。');

/**
 * 收集后台管理端路由权限编码。
 *
 * <p>只提取 `meta.permission` 的字面量，避免把 API、按钮或注释里的权限编码误判为页面访问权限。</p>
 *
 * @param file Vue 路由文件路径
 * @returns 路由权限编码集合
 */
function collectRoutePermissions(file) {
  if (!existsSync(file)) {
    throw new Error(`后台路由文件不存在: ${relative(workspaceRoot, file)}`);
  }
  const source = readFileSync(file, 'utf8');
  const permissions = new Set();
  const permissionPattern = /meta\s*:\s*\{[^}]*\bpermission\s*:\s*['"]([^'"]+)['"][^}]*\}/gms;
  for (const match of source.matchAll(permissionPattern)) {
    permissions.add(match[1]);
  }
  return permissions;
}

/**
 * 收集后端模块元数据声明的权限编码。
 *
 * <p>只读取源码目录下的 `src/main/resources/META-INF/zhyc-module.yml`，避免 target 编译产物影响判断。</p>
 *
 * @param root 后端工程根目录
 * @returns 模块权限编码集合
 */
function collectModulePermissions(root) {
  const yamlFiles = findModuleYamlFiles(root);
  const permissions = new Set();
  for (const file of yamlFiles) {
    for (const permission of parsePermissionsSection(readFileSync(file, 'utf8'))) {
      permissions.add(permission);
    }
  }
  return permissions;
}

/**
 * 查找源码目录中的模块描述文件。
 *
 * @param root 后端工程根目录
 * @returns 模块描述文件路径列表
 */
function findModuleYamlFiles(root) {
  if (!existsSync(root)) {
    throw new Error(`后端工程目录不存在: ${relative(workspaceRoot, root)}`);
  }
  const matchedFiles = [];
  walk(root, (file) => {
    if (file.endsWith('/src/main/resources/META-INF/zhyc-module.yml')) {
      matchedFiles.push(file);
    }
  });
  return matchedFiles;
}

/**
 * 递归遍历文件目录。
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
 * <p>模块描述文件首期使用简单列表格式，本解析器只接受 `permissions:` 下的 `- code` 条目。</p>
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
