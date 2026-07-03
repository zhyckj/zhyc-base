/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const projectRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const routeFile = resolve(projectRoot, 'src/router/routes.ts');
const appFile = resolve(projectRoot, 'src/App.vue');
const pageFile = resolve(projectRoot, 'src/views/system/security-protection/index.vue');

const expectedRoute = '/system/security-protection';
const expectedPermission = 'system:security-protection:query';
const expectedTitle = '安全防护中心';
const expectedComponent = "@/views/system/security-protection/index.vue";
const legacyAccessRestrictionRoute = '/system/access-restrictions';
const accessRestrictionApi = "@/api/system/access-restriction";

const errors = [];
assertFile(routeFile, '后台路由文件');
assertFile(appFile, '后台壳层文件');
assertFile(pageFile, '安全防护中心页面文件');

if (errors.length === 0) {
  const routeSource = readFileSync(routeFile, 'utf8');
  const appSource = readFileSync(appFile, 'utf8');
  const pageSource = readFileSync(pageFile, 'utf8');

  if (!routeSource.includes(`path: '${expectedRoute}'`)) {
    errors.push(`路由表缺少 ${expectedRoute}`);
  }
  if (!routeSource.includes(`title: '${expectedTitle}'`)) {
    errors.push(`路由表缺少标题 ${expectedTitle}`);
  }
  if (!routeSource.includes(`permission: '${expectedPermission}'`)) {
    errors.push(`路由表缺少权限 ${expectedPermission}`);
  }
  if (!routeSource.includes(expectedComponent)) {
    errors.push(`路由表缺少组件 ${expectedComponent}`);
  }
  if (!appSource.includes(`'${expectedRoute}'`)) {
    errors.push(`后台侧边栏分组缺少 ${expectedRoute}`);
  }
  if (routeSource.includes(`path: '${legacyAccessRestrictionRoute}'`)) {
    errors.push(`路由表仍保留独立访问限制入口 ${legacyAccessRestrictionRoute}`);
  }
  if (appSource.includes(`'${legacyAccessRestrictionRoute}'`)) {
    errors.push(`后台侧边栏仍保留独立访问限制入口 ${legacyAccessRestrictionRoute}`);
  }
  if (!pageSource.includes('访问限制规则')) {
    errors.push('安全防护中心页面缺少访问限制规则区块');
  }
  if (!pageSource.includes(accessRestrictionApi)) {
    errors.push(`安全防护中心页面未接入访问限制 API：${accessRestrictionApi}`);
  }
}

if (errors.length > 0) {
  console.error('安全防护中心菜单校验失败。');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log('安全防护中心菜单校验通过。');

function assertFile(file, label) {
  if (!existsSync(file)) {
    errors.push(`${label}不存在：${file}`);
  }
}
