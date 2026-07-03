/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';

const root = resolve(process.argv[2] || process.cwd());
const scanRoot = resolveSystemSourceRoot(root);
const violations = [];

for (const file of listSystemServiceFiles(scanRoot)) {
  const lines = readFileSync(file, 'utf8').split(/\r?\n/);
  lines.forEach((line, index) => {
    if (isAllowedLine(line)) {
      return;
    }
    if (line.includes('throw new IllegalArgumentException(')) {
      violations.push(`${relative(root, file)}:${index + 1} -> 系统服务层不得直接抛裸参数异常: ${line.trim()}`);
    }
  });
}
verifyRequiredBindingProtections(root);

if (violations.length > 0) {
  console.error('系统服务业务异常门禁失败。服务层面向调用方的参数或业务错误必须使用带稳定错误码的 BusinessException：');
  for (const violation of violations) {
    console.error(`- ${violation}`);
  }
  process.exit(1);
}

console.log('系统服务业务异常门禁通过。');

/**
 * 解析系统模块生产源码根目录。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @returns 系统模块生产源码根目录
 */
function resolveSystemSourceRoot(startRoot) {
  const candidates = [
    resolve(startRoot, 'zhyc-module-system/src/main/java/com/zhyc/system'),
    resolve(startRoot, 'zhyc-base-server/zhyc-module-system/src/main/java/com/zhyc/system'),
  ];
  const matchedRoot = candidates.find((candidate) => existsSync(candidate));
  if (matchedRoot) {
    return matchedRoot;
  }
  console.error(`系统服务业务异常门禁失败。未找到系统模块生产源码目录：${candidates.join(' 或 ')}`);
  process.exit(1);
}

/**
 * 递归列出系统模块生产服务源码文件。
 *
 * @param dir 当前扫描目录
 * @returns 服务源码文件路径
 */
function listSystemServiceFiles(dir) {
  const rootStat = statSync(dir);
  if (rootStat.isFile()) {
    return isSystemServiceFile(dir) ? [dir] : [];
  }
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (['target', 'node_modules', '.git'].includes(entry.name)) {
        return [];
      }
      if (path.includes('/src/test/') || path.endsWith('/test')) {
        return [];
      }
      return listSystemServiceFiles(path);
    }
    return entry.isFile() && isSystemServiceFile(path) ? [path] : [];
  });
}

/**
 * 判断文件是否为系统模块生产服务源码文件。
 *
 * @param file 文件路径
 * @returns 属于扫描范围时返回 true
 */
function isSystemServiceFile(file) {
  const normalizedPath = file.split('\\').join('/');
  return normalizedPath.endsWith('.java')
    && normalizedPath.includes('/src/main/java/com/zhyc/system/')
    && normalizedPath.includes('/service/')
    && !normalizedPath.includes('/src/test/')
    && !normalizedPath.includes('/target/');
}

/**
 * 判断源码行是否属于允许跳过的注释内容。
 *
 * @param line 源码行
 * @returns 允许跳过时返回 true
 */
function isAllowedLine(line) {
  const trimmed = line.trim();
  return trimmed.startsWith('//') || trimmed.startsWith('*') || trimmed.startsWith('/*');
}

/**
 * 校验关键绑定服务在替换授权前执行租户归属校验。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 */
function verifyRequiredBindingProtections(startRoot) {
  const requirements = [
    {
      file: 'zhyc-module-system/src/main/java/com/zhyc/system/adminscope/service/DefaultSysAdminScopeService.java',
      snippets: [
        'validateTenantAdminUser(requiredTenantId, requiredUserId);',
        'validateScopeRefs(requiredTenantId, scopes.values());',
        'userRepository.findByTenantId(tenantId)',
        'orgRepository.findByTenantId(tenantId)',
        'moduleRepository.findAll()',
        '管理员用户主键不属于当前租户',
        '租户范围必须等于当前租户',
        '组织范围不属于当前租户',
        '模块范围不存在或未启用',
      ],
      description: '管理员管理范围绑定前必须校验租户用户、租户范围、组织范围和模块范围引用，避免跨租户管理授权',
    },
    {
      file: 'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleService.java',
      snippets: [
        'validateTenantRole(requiredTenantId, requiredRoleId);',
        'validateTenantMenus(requiredTenantId, menuIds);',
        'roleRepository.findByTenantId(tenantId)',
        'menuRepository.findEnabledByTenantId(tenantId)',
        '角色主键不属于当前租户',
        '菜单主键不属于当前租户',
      ],
      description: '角色菜单绑定前必须校验租户角色和菜单归属，避免跨租户角色授权',
    },
    {
      file: 'zhyc-module-system/src/main/java/com/zhyc/system/role/service/DefaultSysRoleDataScopeService.java',
      snippets: [
        'validateTenantRole(requiredTenantId, requiredRoleId);',
        'validateTenantOrgs(requiredTenantId, orgIds);',
        'roleRepository.findByTenantId(tenantId)',
        'orgRepository.findByTenantId(tenantId)',
        '角色主键不属于当前租户',
        '组织主键不属于当前租户',
      ],
      description: '角色数据权限绑定前必须校验租户角色和组织归属，避免跨租户数据范围授权',
    },
    {
      file: 'zhyc-module-system/src/main/java/com/zhyc/system/user/service/DefaultSysUserRoleService.java',
      snippets: [
        'validateTenantRoles(requiredTenantId, bindings.keySet());',
        'roleRepository.findByTenantId(tenantId)',
        '角色主键不属于当前租户',
      ],
      description: '用户角色绑定前必须校验租户角色归属，避免跨租户角色写入用户授权',
    },
    {
      file: 'zhyc-module-system/src/main/java/com/zhyc/system/user/service/DefaultSysUserPostService.java',
      snippets: [
        'validateTenantPosts(requiredTenantId, bindings.keySet());',
        'postRepository.findByTenantIdAndOrgId(tenantId, null)',
        '岗位主键不属于当前租户',
      ],
      description: '用户岗位绑定前必须校验租户岗位归属，避免跨租户岗位写入用户任职',
    },
  ];

  for (const requirement of requirements) {
    const file = resolveRequiredFile(startRoot, requirement.file);
    if (!file) {
      continue;
    }
    const content = readFileSync(file, 'utf8');
    for (const snippet of requirement.snippets) {
      if (!content.includes(snippet)) {
        violations.push(`${relative(startRoot, file)} -> ${requirement.description}，缺少关键片段: ${snippet}`);
      }
    }
  }
}

/**
 * 解析测试工程或真实工程中的指定源码文件。
 *
 * @param startRoot 命令执行根目录或测试工程根目录
 * @param relativeFile 模块内源码相对路径
 * @returns 匹配到的源码文件路径，不存在时返回空
 */
function resolveRequiredFile(startRoot, relativeFile) {
  const candidates = [
    resolve(startRoot, relativeFile),
    resolve(startRoot, 'zhyc-base-server', relativeFile),
  ];
  return candidates.find((candidate) => existsSync(candidate));
}
