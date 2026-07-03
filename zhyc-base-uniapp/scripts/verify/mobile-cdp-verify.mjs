/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import fs from 'node:fs';
import http from 'node:http';

const CDP_VERSION_URL = process.env.MOBILE_CDP_VERSION_URL || 'http://127.0.0.1:9333/json/version';
const MOBILE_BASE_URL = process.env.MOBILE_BASE_URL || 'http://127.0.0.1:9062';
const SCREENSHOT_DIR = process.env.MOBILE_SCREENSHOT_DIR || '/private/tmp';

const AUTH_CONTEXT_STORAGE_KEY = 'ZHYC_MOBILE_USER_CONTEXT';
const VERIFY_USERNAME = process.env.ZHYC_MOBILE_VERIFY_USERNAME || '';
const VERIFY_PASSWORD = process.env.ZHYC_MOBILE_VERIFY_PASSWORD || '';
const VERIFY_ACCOUNT_NAME = process.env.ZHYC_MOBILE_VERIFY_ACCOUNT_NAME || VERIFY_USERNAME;
const VERIFY_TENANT_ID = process.env.ZHYC_MOBILE_VERIFY_TENANT_ID || 'zhyc-platform';
const VERIFY_USER_ID = Number(process.env.ZHYC_MOBILE_VERIFY_USER_ID || 1);
const VERIFY_WITH_AUTH = Boolean(VERIFY_USERNAME && VERIFY_PASSWORD);

const PUBLIC_VERIFY_PAGES = [
  ['login', '/#/pages/auth/login'],
  ['legacy-login', '/#/pages/login/index'],
];

const PROTECTED_VERIFY_PAGES = [
  ['workbench', '/#/pages/workbench/index'],
  ['quick-start', '/#/pages/workbench/quick-start'],
  ['workflow', '/#/pages/workflow/todo'],
  ['workflow-done', '/#/pages/workflow/done'],
  ['workflow-started', '/#/pages/workflow/started'],
  ['workflow-cc', '/#/pages/workflow/cc'],
  ['workflow-detail', '/#/pages/workflow/detail'],
  ['ai', '/#/pages/ai/index'],
  ['message', '/#/pages/message/index'],
  ['purchase-request-form', '/#/pages/purchase/request-form'],
  ['purchase-request-list', '/#/pages/purchase/request-list'],
  ['purchase-order-list', '/#/pages/purchase/order-list'],
  ['purchase-order-detail', '/#/pages/purchase/order-detail'],
  ['purchase-approval-records', '/#/pages/purchase/approval-records'],
  ['mine', '/#/pages/mine/index'],
  ['change-password', '/#/pages/mine/change-password'],
  ['tenant-switch', '/#/pages/mine/tenant-switch'],
  ['login-devices', '/#/pages/mine/login-devices'],
  ['about', '/#/pages/mine/about'],
  ['profile', '/#/pages/profile/index'],
];

const EXPECTED_TAB_LABELS = ['首页', '流程', 'AI', '我的'];
const EXPECTED_WORKBENCH_ENTRY_TITLES = ['流程', '采购', 'AI', '消息'];

let cdpSequence = 1;

function readJson(url) {
  return new Promise((resolve, reject) => {
    http
      .get(url, (response) => {
        let payload = '';
        response.on('data', (chunk) => {
          payload += chunk;
        });
        response.on('end', () => {
          try {
            resolve(JSON.parse(payload));
          } catch (error) {
            reject(error);
          }
        });
      })
      .on('error', reject);
  });
}

async function connectCdp(webSocketDebuggerUrl) {
  const socket = new WebSocket(webSocketDebuggerUrl);
  const pendingRequests = new Map();

  socket.onmessage = (event) => {
    const message = JSON.parse(event.data);
    if (!message.id || !pendingRequests.has(message.id)) {
      return;
    }
    const { resolve, reject } = pendingRequests.get(message.id);
    pendingRequests.delete(message.id);
    if (message.error) {
      reject(new Error(JSON.stringify(message.error)));
      return;
    }
    resolve(message.result);
  };

  await new Promise((resolve, reject) => {
    socket.onopen = resolve;
    socket.onerror = reject;
  });

  return {
    send(method, params = {}, sessionId) {
      const id = cdpSequence++;
      const message = sessionId ? { id, method, params, sessionId } : { id, method, params };
      socket.send(JSON.stringify(message));
      return new Promise((resolve, reject) => {
        pendingRequests.set(id, { resolve, reject });
      });
    },
    close() {
      socket.close();
    },
  };
}

function wait(milliseconds) {
  return new Promise((resolve) => {
    setTimeout(resolve, milliseconds);
  });
}

function assertPageResult(pageResult, options = {}) {
  const failures = [];
  if (pageResult.clientWidth !== 390) {
    failures.push(`视口宽度应为 390，实际为 ${pageResult.clientWidth}`);
  }
  if (pageResult.hasHorizontalOverflow) {
    failures.push(
      `页面存在横向溢出，scrollWidth=${pageResult.scrollWidth}，bodyScrollWidth=${pageResult.bodyScrollWidth}`,
    );
  }
  if (!pageResult.text || pageResult.text.trim().length === 0) {
    failures.push('页面正文为空，可能未渲染成功');
  }

  if (options.expectLoginRedirect && !pageResult.redirectedToLogin) {
    failures.push('未登录访问受保护页面必须跳转到移动端登录页');
  }
  if (options.expectLoginRedirect === false && pageResult.pageName !== 'login' && pageResult.redirectedToLogin) {
    failures.push('已登录验证时不应停留在移动端登录页');
  }

  if (pageResult.pageName !== 'login') {
    const missingTabs = EXPECTED_TAB_LABELS.filter((label) => !pageResult.tabs.includes(label));
    if (missingTabs.length > 0) {
      failures.push(`底部 Tab 缺失：${missingTabs.join('、')}`);
    }
  }

  if (pageResult.pageName === 'workbench' && !pageResult.redirectedToLogin) {
    const appEntryTexts = pageResult.appCells.map((item) => item.text);
    const missingEntries = EXPECTED_WORKBENCH_ENTRY_TITLES.filter((title) => (
      !appEntryTexts.some((text) => text.includes(title))
    ));
    if (missingEntries.length > 0) {
      failures.push(`首页应用入口缺失：${missingEntries.join('、')}`);
    }
    const invisibleEntries = pageResult.appCells
      .filter((item) => !item.visibleInViewport)
      .map((item) => item.text.replace(/\s+/g, ' '));
    if (invisibleEntries.length > 0) {
      failures.push(`首页应用入口被裁切：${invisibleEntries.join('、')}`);
    }
  }

  return failures;
}

async function verifyPage(rootClient, phaseName, pageName, pagePath, authContext = null) {
  const url = new URL(pagePath, MOBILE_BASE_URL).toString();
  const target = await rootClient.send('Target.createTarget', {
    url: new URL('/#/pages/auth/login', MOBILE_BASE_URL).toString(),
  });
  const attached = await rootClient.send('Target.attachToTarget', {
    targetId: target.targetId,
    flatten: true,
  });
  const sessionId = attached.sessionId;
  const send = (method, params = {}) => rootClient.send(method, params, sessionId);

  try {
    await send('Page.enable');
    await send('Runtime.enable');
    await send('Emulation.setDeviceMetricsOverride', {
      width: 390,
      height: 844,
      deviceScaleFactor: 1,
      mobile: true,
      screenWidth: 390,
      screenHeight: 844,
    });
    await send('Emulation.setUserAgentOverride', {
      userAgent:
        'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
    });
    await wait(800);
    if (authContext) {
      await writeAuthContext(send, authContext);
    } else {
      await clearAuthContext(send);
    }
    await send('Page.navigate', { url });
    await wait(2500);

    const evaluation = await send('Runtime.evaluate', {
      returnByValue: true,
      expression: `(() => {
        const viewportWidth = document.documentElement.clientWidth;
        const appCells = Array.from(document.querySelectorAll('.workbench-app-cell')).map((element) => {
          const rect = element.getBoundingClientRect();
          return {
            text: element.innerText,
            left: Math.round(rect.left),
            right: Math.round(rect.right),
            width: Math.round(rect.width),
            visibleInViewport: rect.left >= -1 && rect.right <= viewportWidth + 1
          };
        });
        const tabs = Array.from(document.querySelectorAll('.uni-tabbar__label'))
          .map((element) => element.innerText)
          .filter(Boolean);
        const redirectedToLogin = location.hash.includes('/pages/auth/login');
        return {
          url: location.href,
          title: document.title,
          redirectedToLogin,
          clientWidth: document.documentElement.clientWidth,
          scrollWidth: document.documentElement.scrollWidth,
          bodyScrollWidth: document.body.scrollWidth,
          hasHorizontalOverflow:
            document.documentElement.scrollWidth > document.documentElement.clientWidth + 1 ||
            document.body.scrollWidth > document.documentElement.clientWidth + 1,
          appCells,
          tabs,
          text: document.body.innerText.slice(0, 500)
        };
      })()`,
    });

    const screenshot = await send('Page.captureScreenshot', {
      format: 'png',
      fromSurface: true,
    });
    const file = `${SCREENSHOT_DIR}/zhyc-mobile-${phaseName}-${pageName}-cdp.png`;
    fs.writeFileSync(file, Buffer.from(screenshot.data, 'base64'));

    return {
      pageName,
      screenshot: file,
      ...evaluation.result.value,
    };
  } finally {
    await rootClient.send('Target.closeTarget', { targetId: target.targetId });
  }
}

async function clearAuthContext(send) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      if (globalThis.uni && typeof globalThis.uni.removeStorageSync === 'function') {
        globalThis.uni.removeStorageSync(${JSON.stringify(AUTH_CONTEXT_STORAGE_KEY)});
      }
      localStorage.removeItem(${JSON.stringify(AUTH_CONTEXT_STORAGE_KEY)});
      return true;
    })()`,
  });
}

async function writeAuthContext(send, authContext) {
  await send('Runtime.evaluate', {
    awaitPromise: true,
    returnByValue: true,
    expression: `(() => {
      const userContext = ${JSON.stringify(authContext)};
      if (globalThis.uni && typeof globalThis.uni.setStorageSync === 'function') {
        globalThis.uni.setStorageSync(${JSON.stringify(AUTH_CONTEXT_STORAGE_KEY)}, userContext);
      }
      localStorage.setItem(${JSON.stringify(AUTH_CONTEXT_STORAGE_KEY)}, JSON.stringify({
        type: 'object',
        data: userContext
      }));
      return true;
    })()`,
  });
}

async function seedAuthenticatedContext() {
  const loginUrl = new URL('/auth-center/mobile/auth/login', MOBILE_BASE_URL).toString();
  const response = await fetch(loginUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: VERIFY_USERNAME,
      password: VERIFY_PASSWORD,
    }),
  });
  if (!response.ok) {
    throw new Error(`移动端登录接口 HTTP 状态码：${response.status}`);
  }
  const result = await response.json();
  const loginResult = result?.data?.accessToken || result?.data?.access_token ? result.data : result?.data?.data;
  const accessToken = loginResult?.accessToken || loginResult?.access_token || loginResult?.token;
  if (!result?.success || !accessToken) {
    throw new Error(result?.message || result?.code || '移动端登录接口未返回访问令牌');
  }
  const claims = parseJwtClaims(accessToken);
  const userContext = {
    userId: loginResult.userId || loginResult.user_id || Number(claims.userId || claims.user_id || claims.sub || VERIFY_USER_ID),
    orgId: null,
    accountName: loginResult.accountName || loginResult.account_name || claims.accountName || claims.preferred_username || VERIFY_ACCOUNT_NAME,
    tenantId: loginResult.tenantId || loginResult.tenant_id || claims.tenantId || claims.tenant_id || VERIFY_TENANT_ID,
    roleName: '移动办公用户',
    accessToken,
    loggedIn: true,
  };
  if (!userContext.userId || !userContext.tenantId || !userContext.accountName) {
    throw new Error('移动端登录接口响应缺少用户、租户或账号上下文');
  }
  return {
    accountName: userContext.accountName,
    tenantId: userContext.tenantId,
    userContext,
    hasToken: Boolean(userContext.accessToken),
  };
}

function parseJwtClaims(accessToken) {
  try {
    const base64Payload = accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(Buffer.from(base64Payload, 'base64').toString('utf8'));
  } catch {
    return {};
  }
}

async function main() {
  const browserVersion = await readJson(CDP_VERSION_URL);
  const rootClient = await connectCdp(browserVersion.webSocketDebuggerUrl);
  const results = [];
  try {
    for (const [pageName, pagePath] of PUBLIC_VERIFY_PAGES) {
      const pageResult = await verifyPage(rootClient, 'anonymous', pageName, pagePath);
      const failures = assertPageResult(pageResult);
      results.push({
        phase: 'anonymous',
        ...pageResult,
        passed: failures.length === 0,
        failures,
      });
    }
    for (const [pageName, pagePath] of PROTECTED_VERIFY_PAGES) {
      const pageResult = await verifyPage(rootClient, 'anonymous', pageName, pagePath);
      const failures = assertPageResult(pageResult, { expectLoginRedirect: true });
      results.push({
        phase: 'anonymous',
        ...pageResult,
        passed: failures.length === 0,
        failures,
      });
    }
    if (VERIFY_WITH_AUTH) {
      const authContext = await seedAuthenticatedContext();
      console.log(`移动端登录上下文初始化完成：${authContext.accountName} / ${authContext.tenantId}`);
      for (const [pageName, pagePath] of PROTECTED_VERIFY_PAGES) {
        const pageResult = await verifyPage(rootClient, 'authenticated', pageName, pagePath, authContext.userContext);
        const failures = assertPageResult(pageResult, { expectLoginRedirect: false });
        results.push({
          phase: 'authenticated',
          ...pageResult,
          passed: failures.length === 0,
          failures,
        });
      }
    } else {
      console.log('未提供 ZHYC_MOBILE_VERIFY_USERNAME / ZHYC_MOBILE_VERIFY_PASSWORD，跳过已登录页面渲染验证。');
    }
  } finally {
    rootClient.close();
  }
  console.log(JSON.stringify(results, null, 2));
  const failedResults = results.filter((item) => !item.passed);
  if (failedResults.length > 0) {
    console.error(
      failedResults
        .map((item) => `${item.pageName}: ${item.failures.join('；')}`)
        .join('\n'),
    );
    process.exit(1);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
