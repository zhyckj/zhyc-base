/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import fs from 'node:fs';
import http from 'node:http';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');

const CDP_VERSION_URL = process.env.ADMIN_CDP_VERSION_URL || 'http://127.0.0.1:9333/json/version';
const ADMIN_BASE_URL = process.env.ADMIN_BASE_URL || 'http://127.0.0.1:5173';
const AUTH_LOGIN_URL = process.env.ADMIN_AUTH_LOGIN_URL || 'http://127.0.0.1:8090/mobile/auth/login';
const SCREENSHOT_DIR = process.env.ADMIN_SCREENSHOT_DIR || '/private/tmp';
const CDP_REQUEST_TIMEOUT_MS = Number(process.env.ADMIN_CDP_REQUEST_TIMEOUT_MS || 10_000);
const ADMIN_CONTEXT_STORAGE_KEY = 'ZHYC_ADMIN_RUNTIME_CONTEXT';
const VERIFY_USERNAME = process.env.ZHYC_ADMIN_VERIFY_USERNAME || process.env.ZHYC_MOBILE_VERIFY_USERNAME || '';
const VERIFY_PASSWORD = process.env.ZHYC_ADMIN_VERIFY_PASSWORD || process.env.ZHYC_MOBILE_VERIFY_PASSWORD || '';
const VERIFY_ACCOUNT_NAME = process.env.ZHYC_ADMIN_VERIFY_ACCOUNT_NAME || VERIFY_USERNAME;
const VERIFY_TENANT_ID = process.env.ZHYC_ADMIN_VERIFY_TENANT_ID || 'zhyc-platform';
const VERIFY_USER_ID = Number(process.env.ZHYC_ADMIN_VERIFY_USER_ID || 1);

let cdpSequence = 1;

/**
 * 后台页面验证路由定义。
 *
 * @typedef {Object} AdminVerifyRoute
 * @property {string} name 路由名称。
 * @property {string} path 后台访问路径。
 * @property {string} title 页面标题。
 * @property {boolean} standalone 是否独立页面。
 */

/**
 * 读取 JSON 接口。
 *
 * @param {string} url 请求地址。
 * @returns {Promise<unknown>} JSON 数据。
 */
function readJson(url) {
  return new Promise((resolvePromise, rejectPromise) => {
    http
      .get(url, (response) => {
        let payload = '';
        response.on('data', (chunk) => {
          payload += chunk;
        });
        response.on('end', () => {
          try {
            resolvePromise(JSON.parse(payload));
          } catch (error) {
            rejectPromise(error);
          }
        });
      })
      .on('error', rejectPromise);
  });
}

/**
 * 连接 Chrome DevTools Protocol。
 *
 * @param {string} webSocketDebuggerUrl CDP WebSocket 地址。
 * @returns {Promise<{send: Function, onEvent: Function, close: Function}>} CDP 客户端。
 */
async function connectCdp(webSocketDebuggerUrl) {
  const socket = new WebSocket(webSocketDebuggerUrl);
  const pendingRequests = new Map();
  const eventListeners = new Set();

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (message.id && pendingRequests.has(message.id)) {
      const { resolve: resolvePromise, reject: rejectPromise } = pendingRequests.get(message.id);
      pendingRequests.delete(message.id);
      if (message.error) {
        rejectPromise(new Error(JSON.stringify(message.error)));
        return;
      }
      resolvePromise(message.result);
      return;
    }
    eventListeners.forEach((listener) => listener(message));
  };

  await new Promise((resolvePromise, rejectPromise) => {
    socket.onopen = resolvePromise;
    socket.onerror = rejectPromise;
  });

  return {
    send(method, params = {}, sessionId) {
      const id = cdpSequence++;
      const message = sessionId ? { id, method, params, sessionId } : { id, method, params };
      socket.send(JSON.stringify(message));
      return new Promise((resolvePromise, rejectPromise) => {
        const timeout = setTimeout(() => {
          pendingRequests.delete(id);
          rejectPromise(new Error(`CDP 请求超时：${method}`));
        }, CDP_REQUEST_TIMEOUT_MS);
        pendingRequests.set(id, {
          resolve(result) {
            clearTimeout(timeout);
            resolvePromise(result);
          },
          reject(error) {
            clearTimeout(timeout);
            rejectPromise(error);
          },
        });
      });
    },
    onEvent(listener) {
      eventListeners.add(listener);
      return () => eventListeners.delete(listener);
    },
    close() {
      socket.close();
    },
  };
}

/**
 * 等待指定时长。
 *
 * @param {number} milliseconds 等待毫秒数。
 * @returns {Promise<void>} 等待完成。
 */
function wait(milliseconds) {
  return new Promise((resolvePromise) => {
    setTimeout(resolvePromise, milliseconds);
  });
}

/**
 * 从 Vue 路由源文件解析后台菜单路由。
 *
 * @returns {AdminVerifyRoute[]} 可验证路由清单。
 */
function readAdminRoutes() {
  const routeSource = readFileSync(resolve(rootDir, 'src/router/routes.ts'), 'utf8');
  const routesStart = routeSource.indexOf('export const adminRoutes');
  const routesEnd = routeSource.indexOf('export const router');
  const routesSection = routeSource.slice(routesStart, routesEnd);
  const routeBlocks = routesSection.split(/\n\s*\},\n\s*\{/);
  return routeBlocks
    .map((block) => {
      const path = matchText(block, /path:\s*'([^']+)'/);
      const name = matchText(block, /name:\s*'([^']+)'/) || path.replace(/[^A-Za-z0-9]+/g, '-');
      const title = matchText(block, /title:\s*'([^']+)'/) || name;
      const standalone = /standalone:\s*true/.test(block);
      return { name, path, title, standalone };
    })
    .filter((route) => route.path && !route.path.includes(':') && route.path !== '/auth/callback');
}

/**
 * 匹配文本片段。
 *
 * @param {string} value 原始文本。
 * @param {RegExp} pattern 匹配规则。
 * @returns {string} 匹配结果。
 */
function matchText(value, pattern) {
  const matched = value.match(pattern);
  return matched ? matched[1] : '';
}

/**
 * 校验匿名访问保护结果。
 *
 * @param {Record<string, unknown>} pageResult 页面检查结果。
 * @returns {string[]} 失败原因列表。
 */
function assertAnonymousProtection(pageResult) {
  const failures = [];
  if (!pageResult.redirectedToLogin) {
    failures.push('未登录访问受保护页面没有跳转到登录页');
  }
  if (pageResult.exposesAuthenticatedShell) {
    failures.push('未登录状态暴露了后台菜单或业务页签');
  }
  if (pageResult.hasViteOverlay) {
    failures.push('页面出现 Vite 错误浮层');
  }
  return failures;
}

/**
 * 校验已登录后台页面渲染结果。
 *
 * @param {Record<string, unknown>} pageResult 页面检查结果。
 * @param {AdminVerifyRoute} route 路由定义。
 * @returns {string[]} 失败原因列表。
 */
function assertAuthenticatedPage(pageResult, route) {
  const failures = [];
  if (pageResult.redirectedToLogin) {
    failures.push('已登录后仍停留在登录页');
  }
  if (!pageResult.hasExpectedTitle) {
    failures.push(`页面未展示当前路由标题：${route.title}`);
  }
  if (pageResult.hasViteOverlay) {
    failures.push('页面出现 Vite 错误浮层');
  }
  if (!pageResult.text || String(pageResult.text).trim().length === 0) {
    failures.push('页面正文为空，可能未渲染成功');
  }
  if (!route.standalone && !pageResult.hasLayoutTitle) {
    failures.push('后台基础布局标题缺失');
  }
  if (!route.standalone && !pageResult.hasSearchInput) {
    failures.push('后台顶部搜索框缺失');
  }
  if (pageResult.hasRootHorizontalOverflow) {
    failures.push(`根节点横向溢出，scrollWidth=${pageResult.scrollWidth}，clientWidth=${pageResult.clientWidth}`);
  }
  if (Array.isArray(pageResult.runtimeErrors) && pageResult.runtimeErrors.length > 0) {
    failures.push(`运行时异常：${pageResult.runtimeErrors.slice(0, 2).join('；')}`);
  }
  return failures;
}

/**
 * 验证单个后台页面。
 *
 * @param {ReturnType<typeof connectCdp>} rootClient 根 CDP 客户端。
 * @param {string} phaseName 验证阶段。
 * @param {AdminVerifyRoute} route 路由定义。
 * @param {Record<string, unknown> | null} authContext 后台认证上下文。
 * @returns {Promise<Record<string, unknown>>} 页面验证结果。
 */
async function verifyPage(rootClient, phaseName, route, authContext = null) {
  const url = new URL(route.path, ADMIN_BASE_URL).toString();
  const target = await rootClient.send('Target.createTarget', {
    url: new URL('/login', ADMIN_BASE_URL).toString(),
  });
  const attached = await rootClient.send('Target.attachToTarget', {
    targetId: target.targetId,
    flatten: true,
  });
  const sessionId = attached.sessionId;
  const send = (method, params = {}) => rootClient.send(method, params, sessionId);
  const runtimeErrors = [];
  const unsubscribe = rootClient.onEvent((message) => {
    if (message.sessionId !== sessionId) {
      return;
    }
    if (message.method === 'Runtime.exceptionThrown') {
      runtimeErrors.push(message.params?.exceptionDetails?.text || 'Runtime.exceptionThrown');
    }
    if (message.method === 'Log.entryAdded' && message.params?.entry?.level === 'error') {
      const errorText = message.params.entry.text || 'Log.entryAdded';
      if (!isIgnorableBrowserLog(errorText)) {
        runtimeErrors.push(errorText);
      }
    }
  });

  try {
    await send('Page.enable');
    await send('Runtime.enable');
    await send('Log.enable');
    await send('Emulation.setDeviceMetricsOverride', {
      width: 1440,
      height: 900,
      deviceScaleFactor: 1,
      mobile: false,
      screenWidth: 1440,
      screenHeight: 900,
    });
    await wait(600);
    if (authContext) {
      await writeAdminContext(send, authContext);
    } else {
      await clearAdminContext(send);
    }
    await send('Page.navigate', { url });
    await wait(2600);

    const evaluation = await send('Runtime.evaluate', {
      returnByValue: true,
      expression: `(() => {
        const text = document.body?.innerText || '';
        const clientWidth = document.documentElement.clientWidth;
        const scrollWidth = document.documentElement.scrollWidth;
        const bodyScrollWidth = document.body?.scrollWidth || 0;
        const expectedTitle = ${JSON.stringify(route.title)};
        const hasRootHorizontalOverflow =
          scrollWidth > clientWidth + 1 ||
          bodyScrollWidth > clientWidth + 1;
        return {
          url: location.href,
          title: document.title,
          redirectedToLogin: location.pathname === '/login',
          hasExpectedTitle: text.includes(expectedTitle) || document.title.includes(expectedTitle),
          hasViteOverlay: Boolean(document.querySelector('vite-error-overlay')),
          hasLayoutTitle: Boolean(document.querySelector('.platform-logo')),
          hasSearchInput: Boolean(document.querySelector('input[placeholder="搜索菜单"], .platform-menu-search-input')),
          exposesAuthenticatedShell: Boolean(document.querySelector('.platform-layout, .platform-menu, .platform-page-tabs')),
          clientWidth,
          scrollWidth,
          bodyScrollWidth,
          hasRootHorizontalOverflow,
          text: text.slice(0, 800)
        };
      })()`,
    });
    const blockingRuntimeErrors = runtimeErrors.filter((errorText) => !isIgnorableRuntimeError(errorText));
    const pageResult = {
      phase: phaseName,
      routeName: route.name,
      path: route.path,
      title: route.title,
      runtimeErrors: blockingRuntimeErrors,
      ...evaluation.result.value,
    };
    if (shouldCaptureAll() || blockingRuntimeErrors.length > 0 || pageResult.hasViteOverlay) {
      pageResult.screenshot = await capturePageScreenshot(send, phaseName, route.name);
    }
    return pageResult;
  } finally {
    unsubscribe();
    try {
      await rootClient.send('Target.closeTarget', { targetId: target.targetId });
    } catch (error) {
      console.warn(`关闭 CDP 页面失败：${error.message}`);
    }
  }
}

/**
 * 判断浏览器错误日志是否属于验证无关噪声。
 *
 * @param {string} errorText 浏览器错误日志。
 * @returns {boolean} 可忽略时返回 true。
 */
function isIgnorableBrowserLog(errorText) {
  return /Failed to load resource/i.test(errorText) && /(404|500|net::ERR_ABORTED|favicon)/i.test(errorText);
}

/**
 * 判断运行时异常是否为当前页面可访问性验证可忽略的接口加载噪声。
 *
 * @param {string} errorText 运行时异常文本。
 * @returns {boolean} 可忽略时返回 true。
 */
function isIgnorableRuntimeError(errorText) {
  return /Uncaught \(in promise\)/i.test(errorText);
}

/**
 * 判断是否捕获所有页面截图。
 *
 * @returns {boolean} 需要保留所有页面截图时返回 true。
 */
function shouldCaptureAll() {
  return process.env.ADMIN_CAPTURE_ALL === '1';
}

/**
 * 清理后台登录上下文。
 *
 * @param {Function} send 当前 CDP 会话发送函数。
 * @returns {Promise<void>} 清理完成。
 */
async function clearAdminContext(send) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      localStorage.removeItem(${JSON.stringify(ADMIN_CONTEXT_STORAGE_KEY)});
      return true;
    })()`,
  });
}

/**
 * 写入后台登录上下文。
 *
 * @param {Function} send 当前 CDP 会话发送函数。
 * @param {Record<string, unknown>} authContext 后台登录上下文。
 * @returns {Promise<void>} 写入完成。
 */
async function writeAdminContext(send, authContext) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      localStorage.setItem(${JSON.stringify(ADMIN_CONTEXT_STORAGE_KEY)}, ${JSON.stringify(JSON.stringify(authContext))});
      return true;
    })()`,
  });
}

/**
 * 捕获页面截图。
 *
 * @param {Function} send 当前 CDP 会话发送函数。
 * @param {string} phaseName 验证阶段。
 * @param {string} routeName 路由名称。
 * @returns {Promise<string>} 截图路径。
 */
async function capturePageScreenshot(send, phaseName, routeName) {
  const screenshot = await send('Page.captureScreenshot', {
    format: 'png',
    fromSurface: true,
  });
  const normalizedName = routeName.replace(/[^A-Za-z0-9_-]+/g, '-');
  const file = `${SCREENSHOT_DIR}/zhyc-admin-${phaseName}-${normalizedName}-cdp.png`;
  fs.writeFileSync(file, Buffer.from(screenshot.data, 'base64'));
  return file;
}

/**
 * 调用认证中心登录接口初始化后台验证上下文。
 *
 * @returns {Promise<{accountName: string, tenantId: string, adminContext: Record<string, unknown>}>} 后台验证上下文。
 */
async function seedAuthenticatedContext() {
  if (!VERIFY_USERNAME || !VERIFY_PASSWORD) {
    throw new Error('请提供 ZHYC_ADMIN_VERIFY_USERNAME / ZHYC_ADMIN_VERIFY_PASSWORD 后再执行已登录后台页面验证');
  }
  const response = await fetch(AUTH_LOGIN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: VERIFY_USERNAME,
      password: VERIFY_PASSWORD,
    }),
  });
  if (!response.ok) {
    throw new Error(`后台验证登录接口 HTTP 状态码：${response.status}`);
  }
  const result = await response.json();
  const loginResult = result?.data?.accessToken || result?.data?.access_token ? result.data : result?.data?.data;
  const accessToken = loginResult?.accessToken || loginResult?.access_token || loginResult?.token;
  if (!result?.success || !accessToken) {
    throw new Error(result?.message || result?.code || '后台验证登录接口未返回访问令牌');
  }
  const claims = parseJwtClaims(accessToken);
  const expiresIn = Number(loginResult.expiresIn || loginResult.expires_in || claims.exp || 0);
  const userId = Number(loginResult.userId || loginResult.user_id || claims.userId || claims.user_id || claims.sub || VERIFY_USER_ID);
  const tenantId = String(loginResult.tenantId || loginResult.tenant_id || claims.tenantId || claims.tenant_id || VERIFY_TENANT_ID);
  const accountName = String(
    loginResult.accountName || loginResult.account_name || claims.accountName || claims.preferred_username || VERIFY_ACCOUNT_NAME,
  );
  const adminContext = {
    tenantId,
    userId,
    orgId: null,
    accountName,
    accessToken,
    accessTokenExpiresAt: calculateExpiresAt(expiresIn),
  };
  if (!adminContext.userId || !adminContext.tenantId || !adminContext.accountName) {
    throw new Error('后台验证登录接口响应缺少用户、租户或账号上下文');
  }
  return {
    accountName,
    tenantId,
    adminContext,
  };
}

/**
 * 解析 JWT 载荷。
 *
 * @param {string} accessToken 访问令牌。
 * @returns {Record<string, unknown>} JWT 声明。
 */
function parseJwtClaims(accessToken) {
  try {
    const payload = accessToken.split('.')[1];
    const base64Payload = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(Buffer.from(base64Payload, 'base64').toString('utf8'));
  } catch {
    return {};
  }
}

/**
 * 计算访问令牌过期时间。
 *
 * @param {number} expiresIn 过期秒数或 JWT exp。
 * @returns {number | null} 过期时间戳。
 */
function calculateExpiresAt(expiresIn) {
  if (!Number.isFinite(expiresIn) || expiresIn <= 0) {
    return null;
  }
  if (expiresIn > 4_000_000_000) {
    return expiresIn * 1000;
  }
  return Date.now() + expiresIn * 1000;
}

/**
 * 输出验证摘要。
 *
 * @param {Array<Record<string, unknown>>} results 验证结果。
 */
function printSummary(results) {
  const failedResults = results.filter((item) => !item.passed);
  const summary = {
    total: results.length,
    passed: results.length - failedResults.length,
    failed: failedResults.length,
    failures: failedResults.map((item) => ({
      phase: item.phase,
      path: item.path,
      title: item.title,
      failures: item.failures,
      screenshot: item.screenshot,
    })),
  };
  console.log(JSON.stringify(summary, null, 2));
  if (failedResults.length > 0) {
    console.error(
      failedResults
        .map((item) => `${item.phase} ${item.path}: ${item.failures.join('；')}`)
        .join('\n'),
    );
    process.exit(1);
  }
}

/**
 * 主流程。
 */
async function main() {
  const routes = readAdminRoutes();
  const protectedRoutes = routes.filter((route) => !route.standalone && route.path !== '/');
  const loginRoute = routes.find((route) => route.path === '/login');
  if (!loginRoute) {
    throw new Error('未找到后台登录页路由');
  }
  const browserVersion = await readJson(CDP_VERSION_URL);
  const rootClient = await connectCdp(browserVersion.webSocketDebuggerUrl);
  const results = [];
  try {
    for (const [index, route] of protectedRoutes.entries()) {
      console.log(`[${index + 1}/${protectedRoutes.length}] 验证未登录保护：${route.title} ${route.path}`);
      const anonymousResult = await verifyPage(rootClient, 'anonymous', route);
      const anonymousFailures = assertAnonymousProtection(anonymousResult);
      results.push({
        ...anonymousResult,
        passed: anonymousFailures.length === 0,
        failures: anonymousFailures,
      });
    }

    const authContext = await seedAuthenticatedContext();
    console.log(`后台登录上下文初始化完成：${authContext.accountName} / ${authContext.tenantId}`);
    for (const [index, route] of protectedRoutes.entries()) {
      console.log(`[${index + 1}/${protectedRoutes.length}] 验证后台菜单：${route.title} ${route.path}`);
      const pageResult = await verifyPage(rootClient, 'authenticated', route, authContext.adminContext);
      const failures = assertAuthenticatedPage(pageResult, route);
      if (failures.length > 0 && !pageResult.screenshot) {
        pageResult.screenshot = await reopenAndCaptureFailure(rootClient, route, authContext.adminContext);
      }
      results.push({
        ...pageResult,
        passed: failures.length === 0,
        failures,
      });
    }
  } finally {
    rootClient.close();
  }
  printSummary(results);
}

/**
 * 失败页面重新截图，避免常规验证默认不保留所有截图。
 *
 * @param {ReturnType<typeof connectCdp>} rootClient 根 CDP 客户端。
 * @param {AdminVerifyRoute} route 路由定义。
 * @param {Record<string, unknown>} authContext 后台登录上下文。
 * @returns {Promise<string>} 截图路径。
 */
async function reopenAndCaptureFailure(rootClient, route, authContext) {
  const previousCaptureAll = process.env.ADMIN_CAPTURE_ALL;
  process.env.ADMIN_CAPTURE_ALL = '1';
  try {
    const pageResult = await verifyPage(rootClient, 'failure', route, authContext);
    return pageResult.screenshot;
  } finally {
    if (previousCaptureAll === undefined) {
      delete process.env.ADMIN_CAPTURE_ALL;
    } else {
      process.env.ADMIN_CAPTURE_ALL = previousCaptureAll;
    }
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
