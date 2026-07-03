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
const backendRoot = process.env.ZHYC_MODULE_METADATA_FIXTURE_ROOT
  ? resolve(process.env.ZHYC_MODULE_METADATA_FIXTURE_ROOT)
  : serverRoot;
const yamlFiles = findModuleYamlFiles(backendRoot);
const descriptors = yamlFiles.map((file) => parseDescriptor(file));
const violations = [
  ...findDuplicateModuleCodes(descriptors),
  ...findDuplicateResourceCodes(descriptors, 'menus', '菜单'),
  ...findDuplicateResourceCodes(descriptors, 'permissions', '权限'),
  ...findInvalidResourceCodes(descriptors, 'menus', '菜单'),
  ...findInvalidResourceCodes(descriptors, 'permissions', '权限'),
];

if (violations.length > 0) {
  console.error('模块元数据质量校验失败。');
  violations.forEach((violation) => console.error(`- ${violation}`));
  process.exit(1);
}

console.log('模块元数据质量校验通过。');

/**
 * 查找源码目录中的模块描述文件。
 *
 * @param root 后端工程根目录
 * @returns 模块描述文件路径列表
 */
function findModuleYamlFiles(root) {
  if (!existsSync(root)) {
    throw new Error(`后端工程目录不存在: ${root}`);
  }
  const matchedFiles = [];
  walk(root, (file) => {
    if (file.endsWith('/src/main/resources/META-INF/zhyc-module.yml')) {
      matchedFiles.push(file);
    }
  });
  return matchedFiles.sort();
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
 * 解析模块描述文件中的标量和列表字段。
 *
 * @param file 模块描述文件路径
 * @returns 模块描述摘要
 */
function parseDescriptor(file) {
  const descriptor = {
    file,
    moduleCode: '',
    menus: [],
    permissions: [],
  };
  let activeList = '';
  for (const line of readFileSync(file, 'utf8').split(/\r?\n/)) {
    const scalarMatch = line.match(/^([A-Za-z][A-Za-z0-9_-]*):\s*(.*?)\s*$/);
    if (scalarMatch && !line.startsWith(' ')) {
      const [, key, value] = scalarMatch;
      activeList = value ? '' : key;
      if (key === 'moduleCode') {
        descriptor.moduleCode = value;
      }
      continue;
    }
    const itemMatch = line.match(/^\s*-\s+(.+?)\s*$/);
    if (!itemMatch) {
      continue;
    }
    if (activeList === 'menus') {
      descriptor.menus.push(itemMatch[1]);
    }
    if (activeList === 'permissions') {
      descriptor.permissions.push(itemMatch[1]);
    }
  }
  return descriptor;
}

/**
 * 查找重复模块编码。
 *
 * @param descriptors 模块描述摘要
 * @returns 违规说明列表
 */
function findDuplicateModuleCodes(descriptors) {
  const seen = new Map();
  const violations = [];
  for (const descriptor of descriptors) {
    if (!descriptor.moduleCode) {
      violations.push(`${relative(backendRoot, descriptor.file)} 缺少 moduleCode`);
      continue;
    }
    const owner = seen.get(descriptor.moduleCode);
    if (owner) {
      violations.push(`模块编码重复: ${descriptor.moduleCode} -> ${owner}, ${relative(backendRoot, descriptor.file)}`);
      continue;
    }
    seen.set(descriptor.moduleCode, relative(backendRoot, descriptor.file));
  }
  return violations;
}

/**
 * 查找跨模块重复资源编码。
 *
 * @param descriptors 模块描述摘要
 * @param field 资源字段名
 * @param label 资源中文名称
 * @returns 违规说明列表
 */
function findDuplicateResourceCodes(descriptors, field, label) {
  const seen = new Map();
  const violations = [];
  for (const descriptor of descriptors) {
    const moduleCode = descriptor.moduleCode || relative(backendRoot, descriptor.file);
    const localSeen = new Set();
    for (const code of descriptor[field]) {
      if (localSeen.has(code)) {
        violations.push(`${label}编码在模块内重复: ${code} -> ${moduleCode}`);
      }
      localSeen.add(code);
      const owner = seen.get(code);
      if (owner && owner !== moduleCode) {
        violations.push(`${label}编码跨模块重复: ${code} -> ${owner}, ${moduleCode}`);
      }
      if (!owner) {
        seen.set(code, moduleCode);
      }
    }
  }
  return violations;
}

/**
 * 查找不符合平台编码约束的资源编码。
 *
 * @param descriptors 模块描述摘要
 * @param field 资源字段名
 * @param label 资源中文名称
 * @returns 违规说明列表
 */
function findInvalidResourceCodes(descriptors, field, label) {
  const codePattern = /^[A-Za-z0-9][A-Za-z0-9:_-]*$/;
  return descriptors.flatMap((descriptor) => descriptor[field]
    .filter((code) => !codePattern.test(code))
    .map((code) => `${label}编码格式非法: ${code} -> ${descriptor.moduleCode || relative(backendRoot, descriptor.file)}`));
}
