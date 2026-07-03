/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const routerSource = readFileSync(resolve(root, 'src/router/routes.ts'), 'utf8');
const adminContextSource = readFileSync(resolve(root, 'src/utils/adminContext.ts'), 'utf8');
const appSource = readFileSync(resolve(root, 'src/App.vue'), 'utf8');
const mainSource = readFileSync(resolve(root, 'src/main.ts'), 'utf8');

function assertIncludes(file, expected, message) {
  if (!file.includes(expected)) {
    throw new Error(message);
  }
}

function assertMatches(file, pattern, message) {
  if (!pattern.test(file)) {
    throw new Error(message);
  }
}

assertIncludes(
  adminContextSource,
  'export function hasAuthenticatedAdminContext',
  '后台运行时上下文必须提供统一的已登录判断，避免路由和请求层各写一套认证条件。',
);
assertMatches(
  adminContextSource,
  /Boolean\([\s\S]*context\.accessToken[\s\S]*context\.tenantId[\s\S]*context\.userId !== null/s,
  '已登录判断必须同时校验访问令牌、租户和用户，不能只看账号名称。',
);
assertIncludes(
  routerSource,
  'import { hasAuthenticatedAdminContext }',
  '路由层必须直接读取后台运行时上下文，在业务页面渲染前完成认证判断。',
);
assertIncludes(
  routerSource,
  'router.beforeEach',
  '后台路由必须注册全局前置守卫，未登录不能先渲染菜单和业务内容。',
);
assertMatches(
  routerSource,
  /if\s*\(\s*to\.meta\.standalone\s*\)\s*\{[\s\S]*return true;[\s\S]*\}/,
  '登录页、认证回调和公开独立页必须作为匿名白名单直接放行。',
);
assertMatches(
  routerSource,
  /if\s*\(\s*hasAuthenticatedAdminContext\(\)\s*\)\s*\{[\s\S]*return true;[\s\S]*\}/,
  '业务路由放行前必须先确认存在有效后台登录态。',
);
assertMatches(
  routerSource,
  /return\s*\{[\s\S]*path:\s*'\/login'[\s\S]*returnTo:\s*to\.fullPath[\s\S]*\}/,
  '未登录访问业务路由必须跳转统一登录页，并用 returnTo 保留原访问地址。',
);
assertIncludes(
  appSource,
  'shouldRenderPlatformShell',
  '根组件必须提供后台壳层渲染门禁，避免未登录刷新时先露出菜单。',
);
assertMatches(
  appSource,
  /v-else-if="!shouldRenderPlatformShell"/,
  '未认证业务页不能渲染后台菜单壳层，必须先进入空白跳转态。',
);
assertMatches(
  appSource,
  /function\s+redirectToLoginIfNeeded\(\):\s*void[\s\S]*router\.replace[\s\S]*path:\s*'\/login'[\s\S]*returnTo:\s*route\.fullPath/,
  '根组件必须在未认证业务页主动 replace 到登录页并保留 returnTo。',
);
assertMatches(
  mainSource,
  /router\.isReady\(\)\.then\([\s\S]*app\.mount\('#app'\)/,
  '后台入口必须等待路由初始导航和未登录重定向完成后再挂载，避免直达业务页时出现空白壳层。',
);

console.log('后台路由未登录拦截门禁通过。');
