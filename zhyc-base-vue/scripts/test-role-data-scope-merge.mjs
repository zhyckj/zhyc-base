/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');

function read(relativePath) {
  return readFileSync(resolve(root, relativePath), 'utf8');
}

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertNotIncludes(file, unexpected, message) {
  if (file.includes(unexpected)) {
    throw new Error(message);
  }
}

const routes = read('src/router/routes.ts');
const app = read('src/App.vue');
const rolePage = read('src/views/system/role/index.vue');
const seedSql = read('../zhyc-base-server/zhyc-module-system/src/main/resources/db/V2__system_seed.sql');
const initSql = read('../db/init-zhyc-base-v1.sql');

assertNotIncludes(routes, '/system/role-data-scopes', '角色数据权限不能继续保留独立前端路由');
assertNotIncludes(app, '/system/role-data-scopes', '角色数据权限不能继续保留左侧菜单路径');

assertIncludes(rolePage, 'listSystemRoleDataScopes', '角色管理页必须读取角色自定义数据权限');
assertIncludes(rolePage, 'bindSystemRoleDataScopes', '角色管理页必须保存角色自定义数据权限');
assertIncludes(rolePage, 'listSystemOrgTree', '角色管理页必须加载组织候选项');
assertIncludes(rolePage, 'selectedOrgIds', '角色管理页必须维护已选择的数据权限组织');
assertIncludes(rolePage, "v-permission=\"'system:role:edit'\"", '角色管理页数据权限保存动作必须绑定 system:role:edit 权限');

assertIncludes(seedSql, "'system-role-data-scope', '角色数据权限', 'menu', '/system/role-data-scopes', 'system/role-data-scope/index', 'system:role:query', 130, 'disabled'", '种子脚本必须禁用角色数据权限独立菜单');
assertIncludes(seedSql, "(24101, 'zhyc-platform', 230, 'system-role-data-scope-edit'", '种子脚本必须把角色数据权限按钮挂到角色管理下');
assertIncludes(initSql, "'system-role-data-scope', '角色数据权限', 'menu', '/system/role-data-scopes', 'system/role-data-scope/index', 'system:role:query', 130, 'disabled'", '初始化脚本必须禁用角色数据权限独立菜单');
assertIncludes(initSql, "(24101, 'zhyc-platform', 230, 'system-role-data-scope-edit'", '初始化脚本必须把角色数据权限按钮挂到角色管理下');

console.log('角色数据权限合并门禁通过。');
