/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const roleApiSource = readFileSync(resolve(rootDir, 'src/api/system/role.ts'), 'utf8');
const roleViewSource = readFileSync(resolve(rootDir, 'src/views/system/role/index.vue'), 'utf8');

const checks = [
  {
    passed: /export function listSystemRoleMenuIds/.test(roleApiSource),
    message: '角色 API 缺少 listSystemRoleMenuIds 查询函数。',
  },
  {
    passed: /`\/system\/roles\/\$\{roleId\}\/menus`/.test(roleApiSource) && /query:\s*\{\s*tenantId\s*,?\s*\}/s.test(roleApiSource),
    message: '角色 API 未通过 GET /system/roles/${roleId}/menus?tenantId= 查询授权菜单。',
  },
  {
    passed: /listSystemRoleMenuIds/.test(roleViewSource),
    message: '角色授权弹窗未导入或调用已授权菜单查询函数。',
  },
  {
    passed: /Promise\.all\(\s*\[/s.test(roleViewSource)
      && /listSystemMenuTree\(/.test(roleViewSource)
      && /listSystemRoleMenuIds\(/.test(roleViewSource),
    message: '角色授权弹窗打开时未同时加载菜单树和已绑定菜单 ID。',
  },
  {
    passed: /checkedMenuKeys\.value\s*=\s*roleMenuIds/.test(roleViewSource),
    message: '角色授权弹窗未把已绑定菜单 ID 回写到勾选状态。',
  },
];

const failures = checks.filter((check) => !check.passed);

if (failures.length > 0) {
  console.error('角色授权回显校验失败：');
  failures.forEach((failure) => console.error(`- ${failure.message}`));
  process.exit(1);
}

console.log('角色授权回显校验通过。');
