/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { Buffer } from 'node:buffer';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';
import ts from 'typescript';

const rootDir = resolve(fileURLToPath(new URL('..', import.meta.url)));
const sourcePath = resolve(rootDir, 'src/utils/adminOAuth.ts');
const tokenApiSourcePath = resolve(rootDir, 'src/api/auth/oauth.ts');
const contextSourcePath = resolve(rootDir, 'src/utils/adminContext.ts');
const httpSourcePath = resolve(rootDir, 'src/api/http.ts');

assert.ok(existsSync(sourcePath), '后台 OAuth2 登录工具文件必须存在');
assert.ok(existsSync(tokenApiSourcePath), '后台 OAuth2 换令牌 API 文件必须存在');
assert.ok(existsSync(contextSourcePath), '后台运行时上下文工具文件必须存在');
assert.ok(existsSync(httpSourcePath), '后台 HTTP 请求工具文件必须存在');

const sourceCode = readFileSync(sourcePath, 'utf8');
const transpiled = ts.transpileModule(sourceCode, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const moduleUrl = `data:text/javascript;base64,${Buffer.from(transpiled).toString('base64')}`;
const {
  ADMIN_OAUTH_STATE_STORAGE_KEY,
  ADMIN_OAUTH_CALLBACK_STORAGE_KEY,
  buildAdminOAuthLogoutConfig,
  buildAdminOAuthPasswordLoginConfig,
  createAdminOAuthAuthorizeUrl,
  consumeAdminOAuthCallback,
  saveAdminOAuthCallbackResult,
  submitAdminOAuthLogout,
  submitAdminOAuthPasswordLogin,
} = await import(moduleUrl);
const tokenApiSourceCode = readFileSync(tokenApiSourcePath, 'utf8');
const tokenApiTranspiled = ts.transpileModule(tokenApiSourceCode, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const tokenApiModuleUrl = `data:text/javascript;base64,${Buffer.from(tokenApiTranspiled).toString('base64')}`;
const { exchangeAdminOAuthCode, refreshAdminOAuthToken } = await import(tokenApiModuleUrl);
const contextSourceCode = readFileSync(contextSourcePath, 'utf8');
const contextTranspiled = ts.transpileModule(contextSourceCode, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const contextModuleUrl = `data:text/javascript;base64,${Buffer.from(contextTranspiled).toString('base64')}`;
const {
  clearAdminRuntimeContext,
  getAdminRuntimeContext,
  saveAdminRuntimeContext,
  subscribeAdminRuntimeContextChange,
} = await import(contextModuleUrl);
globalThis.__getAdminRuntimeContext = getAdminRuntimeContext;
globalThis.__clearAdminRuntimeContext = clearAdminRuntimeContext;
globalThis.__saveAdminRuntimeContext = saveAdminRuntimeContext;
globalThis.__refreshAdminOAuthToken = refreshAdminOAuthToken;
const httpSourceCode = readFileSync(httpSourcePath, 'utf8')
  .replace("import { refreshAdminOAuthToken } from '@/api/auth/oauth';", '')
  .replace(/import \{[^}]*AdminRuntimeContext[^}]*\} from '@\/utils\/adminContext';/, '')
  .replace("const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';", "const apiBaseUrl = '/api';");
const httpTranspiled = ts.transpileModule(httpSourceCode, {
  compilerOptions: {
    module: ts.ModuleKind.ES2022,
    target: ts.ScriptTarget.ES2022,
  },
}).outputText;
const httpModuleUrl = `data:text/javascript;base64,${Buffer.from(`const getAdminRuntimeContext = globalThis.__getAdminRuntimeContext;\nconst clearAdminRuntimeContext = globalThis.__clearAdminRuntimeContext;\nconst saveAdminRuntimeContext = globalThis.__saveAdminRuntimeContext;\nconst refreshAdminOAuthToken = globalThis.__refreshAdminOAuthToken;\n${httpTranspiled}`).toString('base64')}`;
const { request } = await import(httpModuleUrl);

assert.equal(typeof createAdminOAuthAuthorizeUrl, 'function', '必须导出 createAdminOAuthAuthorizeUrl 函数');
assert.equal(typeof consumeAdminOAuthCallback, 'function', '必须导出 consumeAdminOAuthCallback 函数');
assert.equal(typeof buildAdminOAuthLogoutConfig, 'function', '必须导出 buildAdminOAuthLogoutConfig 函数');
assert.equal(typeof buildAdminOAuthPasswordLoginConfig, 'function', '必须导出 buildAdminOAuthPasswordLoginConfig 函数');
assert.equal(typeof submitAdminOAuthLogout, 'function', '必须导出 submitAdminOAuthLogout 函数');
assert.equal(typeof submitAdminOAuthPasswordLogin, 'function', '必须导出 submitAdminOAuthPasswordLogin 函数');
assert.equal(typeof exchangeAdminOAuthCode, 'function', '必须导出 exchangeAdminOAuthCode 函数');
assert.equal(typeof refreshAdminOAuthToken, 'function', '必须导出 refreshAdminOAuthToken 函数');
assert.equal(typeof clearAdminRuntimeContext, 'function', '必须导出 clearAdminRuntimeContext 函数');
assert.equal(typeof subscribeAdminRuntimeContextChange, 'function', '必须导出后台上下文变更订阅函数');

function createStorage() {
  const data = new Map();
  return {
    getItem: (key) => data.get(key) ?? null,
    setItem: (key, value) => data.set(key, value),
    removeItem: (key) => data.delete(key),
  };
}

function createWindowMock(localStorage) {
  const listeners = new Map();
  return {
    localStorage,
    location: {
      origin: 'http://127.0.0.1:5173',
      pathname: '/purchase/requests',
      search: '?pageNo=1',
      href: 'http://127.0.0.1:5173/purchase/requests?pageNo=1',
    },
    addEventListener(type, listener) {
      const currentListeners = listeners.get(type) ?? new Set();
      currentListeners.add(listener);
      listeners.set(type, currentListeners);
    },
    removeEventListener(type, listener) {
      listeners.get(type)?.delete(listener);
    },
    dispatchEvent(event) {
      listeners.get(event.type)?.forEach((listener) => listener(event));
      return true;
    },
  };
}

globalThis.CustomEvent ??= class CustomEvent {
  constructor(type, eventInitDict = {}) {
    this.type = type;
    this.detail = eventInitDict.detail;
  }
};

const storage = createStorage();
const authorizeUrl = await createAdminOAuthAuthorizeUrl(
  {
    authorizationEndpoint: 'http://127.0.0.1:8090/oauth2/authorize',
    clientId: 'zhyc-admin-spa',
    redirectUri: 'http://127.0.0.1:5173/auth/callback',
    scope: 'openid profile',
    returnTo: '/dashboard',
  },
  storage,
  () => 'state-fixed',
);
const parsedAuthorizeUrl = new URL(authorizeUrl);

assert.equal(parsedAuthorizeUrl.origin + parsedAuthorizeUrl.pathname, 'http://127.0.0.1:8090/oauth2/authorize');
assert.equal(parsedAuthorizeUrl.searchParams.get('response_type'), 'code');
assert.equal(parsedAuthorizeUrl.searchParams.get('client_id'), 'zhyc-admin-spa');
assert.equal(parsedAuthorizeUrl.searchParams.get('redirect_uri'), 'http://127.0.0.1:5173/auth/callback');
assert.equal(parsedAuthorizeUrl.searchParams.get('scope'), 'openid profile');
assert.equal(parsedAuthorizeUrl.searchParams.get('state'), 'state-fixed');
assert.equal(parsedAuthorizeUrl.searchParams.get('code_challenge_method'), 'S256');
assert.match(parsedAuthorizeUrl.searchParams.get('code_challenge'), /^[A-Za-z0-9_-]{43,}$/u);
const storedOAuthState = JSON.parse(storage.getItem(ADMIN_OAUTH_STATE_STORAGE_KEY));
assert.equal(storedOAuthState.state, 'state-fixed');
assert.equal(storedOAuthState.returnTo, '/dashboard');
assert.match(storedOAuthState.codeVerifier, /^[A-Za-z0-9_-]{43,}$/u);

const callbackResult = consumeAdminOAuthCallback('?code=auth-code-001&state=state-fixed', storage);
assert.deepEqual(callbackResult, {
  code: 'auth-code-001',
  state: 'state-fixed',
  codeVerifier: storedOAuthState.codeVerifier,
  returnTo: '/dashboard',
});
assert.equal(storage.getItem(ADMIN_OAUTH_STATE_STORAGE_KEY), null, '消费回调后必须移除一次性 state');

saveAdminOAuthCallbackResult(callbackResult, storage);
assert.deepEqual(JSON.parse(storage.getItem(ADMIN_OAUTH_CALLBACK_STORAGE_KEY)), callbackResult);

globalThis.window = {
  location: {
    origin: 'http://127.0.0.1:5173',
  },
};
const logoutConfig = buildAdminOAuthLogoutConfig();
assert.deepEqual(logoutConfig, {
  logoutEndpoint: 'http://127.0.0.1:8090/logout',
  postLogoutRedirectUri: 'http://127.0.0.1:5173/login?loggedOut=1',
});
const submittedForms = [];
const documentRef = {
  body: {
    appendChild(form) {
      submittedForms.push(form);
    },
  },
  createElement(tagName) {
    const element = {
      tagName,
      style: {},
      children: [],
      appendChild(child) {
        this.children.push(child);
      },
      submit() {
        this.submitted = true;
      },
    };
    return element;
  },
};
submitAdminOAuthLogout(logoutConfig, documentRef);
assert.equal(submittedForms.length, 1, '登出必须创建并提交一个表单');
assert.equal(submittedForms[0].method, 'post');
assert.equal(submittedForms[0].action, 'http://127.0.0.1:8090/logout');
assert.equal(submittedForms[0].submitted, true);
assert.equal(submittedForms[0].children[0].name, 'post_logout_redirect_uri');
assert.equal(submittedForms[0].children[0].value, 'http://127.0.0.1:5173/login?loggedOut=1');

submittedForms.length = 0;
const passwordLoginConfig = buildAdminOAuthPasswordLoginConfig();
assert.deepEqual(passwordLoginConfig, {
  loginEndpoint: 'http://127.0.0.1:8090/login',
});
submitAdminOAuthPasswordLogin(
  {
    username: 'auth-admin',
    password: 'zhyc-auth-password',
  },
  passwordLoginConfig,
  documentRef,
);
assert.equal(submittedForms.length, 1, '前端账号密码登录必须创建并提交一个表单');
assert.equal(submittedForms[0].method, 'post');
assert.equal(submittedForms[0].action, 'http://127.0.0.1:8090/login');
assert.equal(submittedForms[0].submitted, true);
assert.equal(submittedForms[0].children[0].name, 'username');
assert.equal(submittedForms[0].children[0].value, 'auth-admin');
assert.equal(submittedForms[0].children[1].name, 'password');
assert.equal(submittedForms[0].children[1].value, 'zhyc-auth-password');

const mismatchStorage = createStorage();
mismatchStorage.setItem(ADMIN_OAUTH_STATE_STORAGE_KEY, JSON.stringify({ state: 'expected-state', returnTo: '/dashboard' }));
assert.throws(
  () => consumeAdminOAuthCallback('?code=auth-code-002&state=wrong-state', mismatchStorage),
  /后台统一认证 state 校验失败/,
);
assert.throws(
  () => consumeAdminOAuthCallback('?error=access_denied&error_description=cancelled', createStorage()),
  /统一认证授权失败: access_denied/,
);

globalThis.fetch = async (url, options) => {
  assert.equal(url, '/api/auth/oauth2/token');
  assert.equal(options.method, 'POST');
  assert.equal(options.headers['Content-Type'], 'application/json');
assert.deepEqual(JSON.parse(options.body), {
  code: 'auth-code-003',
  redirectUri: 'http://127.0.0.1:5173/auth/callback',
  codeVerifier: 'pkce-code-verifier-003',
});
  return {
    ok: true,
    async json() {
      return {
        success: true,
        data: {
          accessToken: 'access-token-from-bff',
          refreshToken: 'refresh-token-from-bff',
          tokenType: 'Bearer',
          expiresIn: 3600,
          scope: 'openid profile',
        },
      };
    },
  };
};

const tokenResponse = await exchangeAdminOAuthCode({
  code: 'auth-code-003',
  redirectUri: 'http://127.0.0.1:5173/auth/callback',
  codeVerifier: 'pkce-code-verifier-003',
});
assert.equal(tokenResponse.accessToken, 'access-token-from-bff');
assert.equal(tokenResponse.refreshToken, 'refresh-token-from-bff');

globalThis.fetch = async (url, options) => {
  assert.equal(url, '/api/auth/oauth2/refresh');
  assert.equal(options.method, 'POST');
  assert.equal(options.headers['Content-Type'], 'application/json');
  assert.deepEqual(JSON.parse(options.body), {
    refreshToken: 'refresh-token-from-bff',
  });
  return {
    ok: true,
    async json() {
      return {
        success: true,
        data: {
          accessToken: 'access-token-refreshed-from-bff',
          refreshToken: 'refresh-token-rotated-from-bff',
          tokenType: 'Bearer',
          expiresIn: 1800,
          scope: 'openid profile',
        },
      };
    },
  };
};
const refreshResponse = await refreshAdminOAuthToken({ refreshToken: 'refresh-token-from-bff' });
assert.equal(refreshResponse.accessToken, 'access-token-refreshed-from-bff');
assert.equal(refreshResponse.refreshToken, 'refresh-token-rotated-from-bff');

globalThis.fetch = async (url, options) => {
  assert.equal(url, '/api/auth/oauth2/refresh');
  assert.equal(options.method, 'POST');
  return {
    ok: false,
    status: 500,
    async json() {
      return {
        success: false,
        code: 'AUTH_CENTER_TOKEN_EMPTY',
        message: '认证中心未返回访问令牌',
      };
    },
  };
};
await assert.rejects(
  () => refreshAdminOAuthToken({ refreshToken: 'refresh-token-invalid' }),
  /认证中心未返回访问令牌/,
);

const contextStorage = createStorage();
globalThis.window = createWindowMock(contextStorage);
const observedRuntimeContexts = [];
const unsubscribeRuntimeContext = subscribeAdminRuntimeContextChange((context) => {
  observedRuntimeContexts.push(context);
});
saveAdminRuntimeContext({
  tenantId: 'tenant_a',
  userId: 1001,
  orgId: 10,
  accountName: '平台管理员',
  accessToken: 'access-token-expired',
});
assert.equal(observedRuntimeContexts.at(-1)?.accountName, '平台管理员', '保存运行时上下文后必须通知当前页面刷新账号状态');
clearAdminRuntimeContext(contextStorage);
assert.equal(observedRuntimeContexts.at(-1)?.accountName, '未登录', '清理运行时上下文后必须通知当前页面刷新未登录状态');
unsubscribeRuntimeContext();
assert.deepEqual(getAdminRuntimeContext(), {
  tenantId: '',
  userId: null,
  orgId: null,
  accountName: '未登录',
  accessToken: undefined,
  refreshToken: undefined,
  accessTokenExpiresAt: null,
});

saveAdminRuntimeContext({
  tenantId: 'tenant_a',
  userId: 1001,
  orgId: 10,
  accountName: '平台管理员',
  accessToken: 'access-token-expired',
  refreshToken: 'refresh-token-stored',
  accessTokenExpiresAt: Date.now() - 1000,
});
globalThis.__getAdminRuntimeContext = getAdminRuntimeContext;
let refreshThenBusinessCallCount = 0;
globalThis.fetch = async (url, options) => {
  refreshThenBusinessCallCount += 1;
  if (refreshThenBusinessCallCount === 1) {
    assert.equal(url, '/api/auth/oauth2/refresh');
    assert.deepEqual(JSON.parse(options.body), { refreshToken: 'refresh-token-stored' });
    return {
      ok: true,
      async json() {
        return {
          success: true,
          data: {
            accessToken: 'access-token-refreshed-before-request',
            refreshToken: 'refresh-token-stored',
            tokenType: 'Bearer',
            expiresIn: 1800,
            scope: 'openid profile',
          },
        };
      },
    };
  }
  assert.equal(url.href, 'http://127.0.0.1:5173/api/system/users');
  assert.equal(options.headers.Authorization, 'Bearer access-token-refreshed-before-request');
  return {
    ok: true,
    async json() {
      return {
        success: true,
        data: [{ userId: 1001 }],
      };
    },
  };
};
const refreshedBusinessData = await request('/system/users');
assert.deepEqual(refreshedBusinessData, [{ userId: 1001 }]);
assert.equal(getAdminRuntimeContext().accessToken, 'access-token-refreshed-before-request');
assert.equal(refreshThenBusinessCallCount, 2, '过期访问令牌必须先刷新再发起业务请求');

saveAdminRuntimeContext({
  tenantId: 'tenant_a',
  userId: 1001,
  orgId: 10,
  accountName: '平台管理员',
  accessToken: 'access-token-expired-concurrent',
  refreshToken: 'refresh-token-concurrent',
  accessTokenExpiresAt: Date.now() - 1000,
});
let concurrentRefreshCallCount = 0;
const concurrentBusinessUrls = [];
globalThis.fetch = async (url, options) => {
  if (url === '/api/auth/oauth2/refresh') {
    concurrentRefreshCallCount += 1;
    assert.deepEqual(JSON.parse(options.body), { refreshToken: 'refresh-token-concurrent' });
    await new Promise((resolvePromise) => setTimeout(resolvePromise, 0));
    return {
      ok: true,
      async json() {
        return {
          success: true,
          data: {
            accessToken: 'access-token-refreshed-once',
            refreshToken: 'refresh-token-concurrent-rotated',
            tokenType: 'Bearer',
            expiresIn: 1800,
            scope: 'openid profile',
          },
        };
      },
    };
  }
  concurrentBusinessUrls.push(url.href);
  assert.equal(options.headers.Authorization, 'Bearer access-token-refreshed-once');
  return {
    ok: true,
    async json() {
      return {
        success: true,
        data: { path: url.pathname },
      };
    },
  };
};
const [concurrentUsersData, concurrentRolesData] = await Promise.all([
  request('/system/users'),
  request('/system/roles'),
]);
assert.equal(concurrentRefreshCallCount, 1, '并发业务请求必须合并为一次令牌刷新');
assert.deepEqual(concurrentBusinessUrls.sort(), [
  'http://127.0.0.1:5173/api/system/roles',
  'http://127.0.0.1:5173/api/system/users',
]);
assert.deepEqual(concurrentUsersData, { path: '/api/system/users' });
assert.deepEqual(concurrentRolesData, { path: '/api/system/roles' });
assert.equal(getAdminRuntimeContext().refreshToken, 'refresh-token-concurrent-rotated');

saveAdminRuntimeContext({
  tenantId: 'tenant_a',
  userId: 1001,
  orgId: 10,
  accountName: '平台管理员',
  accessToken: 'access-token-valid',
  refreshToken: 'refresh-token-current',
  accessTokenExpiresAt: Date.now() + 600_000,
});
globalThis.fetch = async () => ({
  ok: false,
  status: 500,
  async json() {
    return {
      success: false,
      code: 'SYSTEM_ERROR',
      message: '系统繁忙，请稍后重试',
    };
  },
});
await assert.rejects(() => request('/system/security-protection/overview'), /系统繁忙，请稍后重试/);

saveAdminRuntimeContext({
  tenantId: 'tenant_a',
  userId: 1001,
  orgId: 10,
  accountName: '平台管理员',
  accessToken: 'access-token-expired',
});
globalThis.fetch = async () => ({
  ok: false,
  status: 401,
  async json() {
    return {};
  },
});
await assert.rejects(() => request('/system/users'), /后台登录态已失效，请重新登录/);
assert.equal(getAdminRuntimeContext().accessToken, undefined, '401 后必须清理本地访问令牌');
assert.equal(
  window.location.href,
  'http://127.0.0.1:5173/login?returnTo=%2Fpurchase%2Frequests%3FpageNo%3D1',
  '401 后必须带原路径跳转统一登录页',
);

console.log('后台 OAuth2 登录工具测试通过。');
