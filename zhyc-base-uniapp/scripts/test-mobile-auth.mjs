/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));

function readRequired(relativePath) {
  const filePath = resolve(rootDir, relativePath);
  assert.ok(existsSync(filePath), `${relativePath} 必须存在`);
  return readFileSync(filePath, 'utf8');
}

function readFilesRecursively(relativeDir) {
  const dirPath = resolve(rootDir, relativeDir);
  assert.ok(existsSync(dirPath), `${relativeDir} 必须存在`);
  return readdirSync(dirPath).flatMap((entry) => {
    const fullPath = resolve(dirPath, entry);
    const relativePath = `${relativeDir}/${entry}`;
    if (statSync(fullPath).isDirectory()) {
      return readFilesRecursively(relativePath);
    }
    return [{ relativePath, content: readFileSync(fullPath, 'utf8') }];
  });
}

const pagesJson = readRequired('src/pages.json');
const loginPage = readRequired('src/pages/auth/login.vue');
const requestApi = readRequired('src/api/request.ts');
const authApi = readRequired('src/api/auth.ts');
const runtimeConfig = readRequired('src/config/runtime.ts');
const platformUtil = readRequired('src/utils/platform.ts');
const workbenchPage = readRequired('src/pages/workbench/index.vue');
const mobileDesign = readRequired('src/styles/mobile-design.css');
const viteConfig = readRequired('vite.config.ts');
const pageAndComponentSources = [
  ...readFilesRecursively('src/pages'),
  ...readFilesRecursively('src/components'),
];
const pagesConfig = JSON.parse(pagesJson);
const publicPagePaths = new Set(['pages/auth/login', 'pages/login/index']);
const protectedPagePaths = pagesConfig.pages
  .map((page) => page.path)
  .filter((path) => !publicPagePaths.has(path));
const publicPageSetDefinition = platformUtil.match(
  /const MOBILE_PUBLIC_PAGE_PATHS = new Set\(\[[\s\S]*?\]\);/u,
)?.[0] || '';

assert.match(pagesJson, /pages\/auth\/login/u, '移动端登录页必须登记到 pages.json');
assert.match(loginPage, /loginMobileAccount/u, '登录页必须调用移动端登录 API');
assert.match(loginPage, /saveMobileUserContext/u, '登录成功必须写入统一移动端上下文');
assert.match(loginPage, /redirectAfterMobileLogin/u, '登录成功必须回到登录前页面');
assert.doesNotMatch(loginPage, /window\.|document\.|localStorage|sessionStorage/u, '登录页不得依赖 H5 浏览器专属 API');
assert.doesNotMatch(loginPage, /DEFAULT_DEV_PASSWORD|local_auth_user_password/u, '登录页不得硬编码或预填默认密码');
assert.match(loginPage, /username:\s*''/u, '登录页账号输入框默认必须为空');
assert.match(loginPage, /password:\s*''/u, '登录页密码输入框默认必须为空');
assert.match(loginPage, /decodeQueryText/u, '登录页必须兼容 H5 重定向参数编码');
assert.match(loginPage, /decodeURIComponent/u, '登录页必须解码 returnTo 和 reason 参数');
assert.match(loginPage, /normalizedValue\.startsWith\('\/pages\/'\)/u, '登录页返回地址必须限制为站内页面');

assert.match(authApi, /\/mobile\/auth\/login/u, '移动端登录 API 必须调用认证中心移动登录端点');
assert.match(authApi, /auth:\s*false/u, '登录接口不得要求已有移动端登录态');
assert.match(runtimeConfig, /VITE_MOBILE_AUTH_API_BASE_URL/u, '认证中心地址必须通过运行配置集中管理');
assert.match(runtimeConfig, /VITE_MOBILE_PLATFORM_API_BASE_URL/u, '核心平台地址必须通过运行配置集中管理');

assert.match(requestApi, /auth\?:\s*boolean/u, '移动请求必须支持匿名登录接口');
assert.match(requestApi, /redirectToMobileLogin/u, '移动请求未认证时必须统一跳转登录页');
assert.match(requestApi, /statusCode\s*===\s*401\s*\|\|\s*result\.statusCode\s*===\s*403/u, '401/403 必须触发登录拦截');
assert.match(requestApi, /PERMISSION_DENIED/u, '后端业务权限拒绝响应必须触发登录拦截');
assert.match(platformUtil, /redirectToMobileLogin/u, '移动端必须提供统一登录跳转工具');
assert.match(platformUtil, /redirectAfterMobileLogin/u, '移动端必须提供登录后返回工具');
assert.match(platformUtil, /guardMobileAuthenticatedPage/u, '移动端必须提供统一页面登录守卫');
assert.match(platformUtil, /MOBILE_PUBLIC_PAGE_PATHS/u, '移动端必须集中维护未登录公开页面白名单');
assert.match(platformUtil, /resolveH5HashPagePath/u, 'H5 直达页面必须能从 hash 解析真实路由');
assert.match(readRequired('src/main.ts'), /guardMobileAuthenticatedPage/u, '移动端入口必须全局接入页面登录守卫');
assert.ok(protectedPagePaths.length > 0, '移动端必须至少存在一个受保护业务页面');
assert.ok(publicPageSetDefinition, '移动端公开页面白名单必须集中定义');
for (const pagePath of protectedPagePaths) {
  assert.doesNotMatch(
    publicPageSetDefinition,
    new RegExp(`['"]/${pagePath.replace(/[.*+?^${}()|[\]\\]/gu, '\\$&')}['"]`),
    `${pagePath} 不得加入未登录公开白名单`,
  );
}

assert.doesNotMatch(workbenchPage, /iconText/u, '工作台不得继续使用文字图标字段');
assert.doesNotMatch(workbenchPage, /<view class="workbench-avatar mini-avatar">\{\{/u, '工作台头像不得使用文字图标');
assert.match(workbenchPage, /goLogin/u, '工作台未登录提示必须提供去登录操作');
for (const source of pageAndComponentSources) {
  assert.doesNotMatch(source.content, /iconText/u, `${source.relativePath} 不得继续使用文字图标字段`);
  assert.doesNotMatch(source.content, /mobile-list-icon[^>]*>[^<\s]+</u, `${source.relativePath} 列表图标不得使用单字文本`);
}
assert.doesNotMatch(mobileDesign, /content:\s*'首'|content:\s*'流'|content:\s*'我'/u, '底部 tab 不得通过伪元素输出文字图标');
assert.match(mobileDesign, /mobile-solid-icon/u, '移动端必须使用 CSS 实心图形替代文字图标');

assert.match(viteConfig, /\/auth-center/u, 'H5 本地开发必须代理认证中心登录接口');
assert.match(viteConfig, /127\.0\.0\.1:8081/u, 'H5 本地开发必须代理核心平台 API');

console.log('移动端登录链路门禁通过。');
