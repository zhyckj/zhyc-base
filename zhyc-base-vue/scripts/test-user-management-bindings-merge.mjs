/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert/strict';

const webRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const workspaceRoot = resolve(webRoot, '..');

const userPage = readProjectFile('src/views/system/user/index.vue');
const routes = readProjectFile('src/router/routes.ts');
const appShell = readProjectFile('src/App.vue');
const systemSeed = readWorkspaceFile('zhyc-base-server/zhyc-module-system/src/main/resources/db/V2__system_seed.sql');
const rootInit = readWorkspaceFile('db/init-zhyc-base-v1.sql');

assertIncludes(userPage, 'listSystemUserPosts', '用户管理页必须读取当前用户岗位绑定');
assertIncludes(userPage, 'bindSystemUserPosts', '用户管理页必须保存当前用户岗位绑定');
assertIncludes(userPage, 'listSystemUserRoles', '用户管理页必须读取当前用户角色绑定');
assertIncludes(userPage, 'bindSystemUserRoles', '用户管理页必须保存当前用户角色绑定');
assertIncludes(userPage, 'listSystemPosts', '用户管理页必须加载岗位候选项');
assertIncludes(userPage, 'listSystemRoles', '用户管理页必须加载角色候选项');
assertIncludes(userPage, '角色与岗位', '用户编辑弹窗必须内嵌角色与岗位维护区域');
assertIncludes(userPage, "v-permission=\"'system:user:edit'\"", '用户角色岗位维护区必须绑定 system:user:edit 权限');

assertDoesNotInclude(routes, "path: '/system/user-posts'", '用户岗位独立路由必须下线');
assertDoesNotInclude(routes, "path: '/system/user-roles'", '用户角色独立路由必须下线');
assertDoesNotInclude(appShell, "'/system/user-posts'", '侧边栏不应继续展示用户岗位独立入口');
assertDoesNotInclude(appShell, "'/system/user-roles'", '侧边栏不应继续展示用户角色独立入口');

for (const sql of [systemSeed, rootInit]) {
  assertIncludes(sql, "(223, 'zhyc-platform', 200, 'system-user-post', '用户岗位', 'menu', '/system/user-posts', 'system/user-post/index', 'system:user:query', 90, 'disabled')", '用户岗位旧菜单必须禁用');
  assertIncludes(sql, "(224, 'zhyc-platform', 200, 'system-user-role', '用户角色', 'menu', '/system/user-roles', 'system/user-role/index', 'system:user:query', 100, 'disabled')", '用户角色旧菜单必须禁用');
  assertIncludes(sql, "(22301, 'zhyc-platform', 222, 'system-user-post-bind', '绑定用户岗位', 'button', NULL, NULL, 'system:user:edit', 6, 'enabled')", '用户岗位绑定按钮必须挂到用户管理下');
  assertIncludes(sql, "(22401, 'zhyc-platform', 222, 'system-user-role-bind', '绑定用户角色', 'button', NULL, NULL, 'system:user:edit', 7, 'enabled')", '用户角色绑定按钮必须挂到用户管理下');
}

console.log('用户管理角色岗位合并门禁通过。');

/**
 * 读取后台管理端文件。
 *
 * @param path 项目相对路径
 * @returns 文件内容
 */
function readProjectFile(path) {
  return readFileSync(resolve(webRoot, path), 'utf8');
}

/**
 * 读取工作区文件。
 *
 * @param path 工作区相对路径
 * @returns 文件内容
 */
function readWorkspaceFile(path) {
  return readFileSync(resolve(workspaceRoot, path), 'utf8');
}

/**
 * 断言文本包含指定内容。
 *
 * @param actual 实际文本
 * @param expected 期望片段
 * @param message 失败提示
 */
function assertIncludes(actual, expected, message) {
  assert.ok(actual.includes(expected), message);
}

/**
 * 断言文本不包含指定内容。
 *
 * @param actual 实际文本
 * @param expected 禁止出现的片段
 * @param message 失败提示
 */
function assertDoesNotInclude(actual, expected, message) {
  assert.ok(!actual.includes(expected), message);
}
