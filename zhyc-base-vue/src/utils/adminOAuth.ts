/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 后台 OAuth2 授权请求配置。
 */
export interface AdminOAuthAuthorizeConfig {
  /** 认证中心授权端点，例如 http://127.0.0.1:8090/oauth2/authorize。 */
  authorizationEndpoint: string;
  /** 后台管理端 OAuth2 客户端标识，不包含客户端密钥。 */
  clientId: string;
  /** 后台管理端授权码回调地址。 */
  redirectUri: string;
  /** 授权范围；首期默认 openid profile。 */
  scope?: string;
  /** 认证完成后返回的后台相对路径。 */
  returnTo?: string;
}

/**
 * 后台 OAuth2 登出请求配置。
 */
export interface AdminOAuthLogoutConfig {
  /** 认证中心登出端点，例如 http://127.0.0.1:8090/logout。 */
  logoutEndpoint: string;
  /** 认证中心登出成功后回跳的后台登录页地址。 */
  postLogoutRedirectUri: string;
}

/**
 * 后台 OAuth2 前端登录表单提交配置。
 */
export interface AdminOAuthPasswordLoginConfig {
  /** 认证中心表单登录处理端点，例如 http://127.0.0.1:8090/login。 */
  loginEndpoint: string;
}

/**
 * 后台 OAuth2 前端登录表单命令。
 */
export interface AdminOAuthPasswordLoginCommand {
  /** 认证中心登录账号。 */
  username: string;
  /** 认证中心登录密码。 */
  password: string;
}

/**
 * 后台 OAuth2 回调结果。
 */
export interface AdminOAuthCallbackResult {
  /** 认证中心返回的授权码；必须交给受控后端换取令牌。 */
  code: string;
  /** 回调携带的 state；用于防止跨站请求伪造。 */
  state: string;
  /** PKCE 原始校验码；只随授权码提交给核心平台 BFF 换取令牌。 */
  codeVerifier: string;
  /** 认证完成后返回的后台相对路径。 */
  returnTo: string;
}

/**
 * 最小 Storage 接口，便于浏览器运行和脚本测试共用。
 */
export interface AdminOAuthStorage {
  /** 按键读取文本值。 */
  getItem(key: string): string | null;
  /** 按键写入文本值。 */
  setItem(key: string, value: string): void;
  /** 按键删除文本值。 */
  removeItem(key: string): void;
}

/** 后台统一认证一次性 state 会话缓存键。 */
export const ADMIN_OAUTH_STATE_STORAGE_KEY = 'ZHYC_ADMIN_OAUTH_STATE';

/** 后台统一认证回调结果会话缓存键。 */
export const ADMIN_OAUTH_CALLBACK_STORAGE_KEY = 'ZHYC_ADMIN_OAUTH_CALLBACK';

/**
 * 创建后台 OAuth2 授权地址。
 *
 * <p>该函数只生成授权码入口并保存一次性 state，不在前端保存客户端密钥；授权码换令牌必须通过受控后端完成。</p>
 *
 * @param config 授权请求配置
 * @param storage 会话存储，默认使用浏览器 sessionStorage
 * @param stateFactory state 生成器，测试时可注入稳定值
 * @returns 可跳转的认证中心授权地址
 */
export async function createAdminOAuthAuthorizeUrl(
  config: AdminOAuthAuthorizeConfig,
  storage: AdminOAuthStorage = window.sessionStorage,
  stateFactory: () => string = createOAuthState,
): Promise<string> {
  const authorizationEndpoint = requireText(config.authorizationEndpoint, '认证中心授权端点不能为空');
  const clientId = requireText(config.clientId, '后台 OAuth2 客户端标识不能为空');
  const redirectUri = requireText(config.redirectUri, '后台 OAuth2 回调地址不能为空');
  const state = requireText(stateFactory(), '后台 OAuth2 state 不能为空');
  const returnTo = normalizeReturnTo(config.returnTo);
  const codeVerifier = createPkceCodeVerifier();
  const codeChallenge = await createPkceCodeChallenge(codeVerifier);

  storage.setItem(ADMIN_OAUTH_STATE_STORAGE_KEY, JSON.stringify({ state, returnTo, codeVerifier }));

  const authorizeUrl = new URL(authorizationEndpoint);
  authorizeUrl.searchParams.set('response_type', 'code');
  authorizeUrl.searchParams.set('client_id', clientId);
  authorizeUrl.searchParams.set('redirect_uri', redirectUri);
  authorizeUrl.searchParams.set('scope', normalizeText(config.scope) || 'openid profile');
  authorizeUrl.searchParams.set('state', state);
  authorizeUrl.searchParams.set('code_challenge', codeChallenge);
  authorizeUrl.searchParams.set('code_challenge_method', 'S256');
  return authorizeUrl.toString();
}

/**
 * 消费后台 OAuth2 回调参数。
 *
 * <p>成功消费后会删除一次性 state，防止授权回调被重复使用。</p>
 *
 * @param search 浏览器地址栏查询参数
 * @param storage 会话存储，默认使用浏览器 sessionStorage
 * @returns 已校验 state 的授权码结果
 */
export function consumeAdminOAuthCallback(
  search: string,
  storage: AdminOAuthStorage = window.sessionStorage,
): AdminOAuthCallbackResult {
  const callbackParams = new URLSearchParams(search.startsWith('?') ? search.slice(1) : search);
  const errorCode = normalizeText(callbackParams.get('error'));
  if (errorCode) {
    throw new Error(`统一认证授权失败: ${errorCode}`);
  }

  const code = requireText(callbackParams.get('code'), '统一认证授权码缺失');
  const state = requireText(callbackParams.get('state'), '统一认证 state 缺失');
  const storedState = readStoredState(storage);
  if (!storedState || storedState.state !== state) {
    throw new Error('后台统一认证 state 校验失败');
  }
  storage.removeItem(ADMIN_OAUTH_STATE_STORAGE_KEY);
  return {
    code,
    state,
    codeVerifier: storedState.codeVerifier,
    returnTo: storedState.returnTo,
  };
}

/**
 * 保存后台 OAuth2 回调结果。
 *
 * @param result 已校验的回调结果
 * @param storage 会话存储，默认使用浏览器 sessionStorage
 */
export function saveAdminOAuthCallbackResult(
  result: AdminOAuthCallbackResult,
  storage: AdminOAuthStorage = window.sessionStorage,
): void {
  storage.setItem(ADMIN_OAUTH_CALLBACK_STORAGE_KEY, JSON.stringify(result));
}

/**
 * 从 Vite 环境变量构建后台 OAuth2 配置。
 *
 * @param returnTo 登录成功后的后台相对路径
 * @returns 后台 OAuth2 授权请求配置
 */
export function buildAdminOAuthConfig(returnTo: string): AdminOAuthAuthorizeConfig {
  return {
    authorizationEndpoint: import.meta.env?.VITE_AUTH_AUTHORIZATION_ENDPOINT || 'http://127.0.0.1:8090/oauth2/authorize',
    clientId: import.meta.env?.VITE_AUTH_CLIENT_ID || 'zhyc-auth-client',
    redirectUri: import.meta.env?.VITE_AUTH_REDIRECT_URI || `${window.location.origin}/auth/callback`,
    scope: import.meta.env?.VITE_AUTH_SCOPE || 'openid profile',
    returnTo,
  };
}

/**
 * 从 Vite 环境变量构建后台 OAuth2 登出配置。
 *
 * @returns 后台 OAuth2 登出请求配置
 */
export function buildAdminOAuthLogoutConfig(): AdminOAuthLogoutConfig {
  return {
    logoutEndpoint: import.meta.env?.VITE_AUTH_LOGOUT_ENDPOINT || 'http://127.0.0.1:8090/logout',
    postLogoutRedirectUri: import.meta.env?.VITE_AUTH_POST_LOGOUT_REDIRECT_URI
      || `${window.location.origin}/login?loggedOut=1`,
  };
}

/**
 * 从 Vite 环境变量构建前端账号密码登录提交配置。
 *
 * @returns 认证中心表单登录提交配置
 */
export function buildAdminOAuthPasswordLoginConfig(): AdminOAuthPasswordLoginConfig {
  return {
    loginEndpoint: import.meta.env?.VITE_AUTH_LOGIN_ENDPOINT || 'http://127.0.0.1:8090/login',
  };
}

/**
 * 向认证中心提交账号密码登录表单。
 *
 * <p>这里使用原生表单提交而不是 fetch，确保浏览器直接进入认证中心会话流程并携带认证中心 Cookie；
 * 客户端密钥仍只保存在受控后端，前端仅提交用户输入的账号密码。</p>
 *
 * @param command 账号密码登录命令
 * @param config 登录提交配置
 * @param documentRef 当前页面 Document，测试时可注入
 */
export function submitAdminOAuthPasswordLogin(
  command: AdminOAuthPasswordLoginCommand,
  config: AdminOAuthPasswordLoginConfig,
  documentRef: Document = document,
): void {
  const loginEndpoint = requireText(config.loginEndpoint, '认证中心登录提交端点不能为空');
  const username = requireText(command.username, '请输入认证中心账号');
  const password = requireText(command.password, '请输入认证中心密码');
  const loginForm = documentRef.createElement('form');
  loginForm.method = 'post';
  loginForm.action = loginEndpoint;
  loginForm.style.display = 'none';
  appendHiddenInput(documentRef, loginForm, 'username', username);
  appendHiddenInput(documentRef, loginForm, 'password', password);
  documentRef.body.appendChild(loginForm);
  loginForm.submit();
}

/**
 * 提交认证中心登出请求。
 *
 * <p>Spring Security 默认通过 POST 登出；这里使用顶层表单提交，确保浏览器携带认证中心会话 Cookie，
 * 同时避免前端通过 fetch 读取跨域响应。</p>
 *
 * @param config 登出请求配置
 * @param documentRef 当前页面 Document，测试时可注入
 */
export function submitAdminOAuthLogout(
  config: AdminOAuthLogoutConfig,
  documentRef: Document = document,
): void {
  const logoutEndpoint = requireText(config.logoutEndpoint, '认证中心登出端点不能为空');
  const postLogoutRedirectUri = requireText(config.postLogoutRedirectUri, '认证中心登出回跳地址不能为空');
  const logoutForm = documentRef.createElement('form');
  logoutForm.method = 'post';
  logoutForm.action = logoutEndpoint;
  logoutForm.style.display = 'none';
  appendHiddenInput(documentRef, logoutForm, 'post_logout_redirect_uri', postLogoutRedirectUri);
  documentRef.body.appendChild(logoutForm);
  logoutForm.submit();
}

/**
 * 追加隐藏表单字段。
 *
 * @param documentRef 当前页面 Document
 * @param form 目标表单
 * @param name 字段名
 * @param value 字段值
 */
function appendHiddenInput(documentRef: Document, form: HTMLFormElement, name: string, value: string): void {
  const input = documentRef.createElement('input');
  input.type = 'hidden';
  input.name = name;
  input.value = value;
  form.appendChild(input);
}

/**
 * 创建不可预测的 OAuth2 state。
 *
 * @returns 随机 state 文本
 */
function createOAuthState(): string {
  const bytes = new Uint8Array(16);
  if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
    crypto.getRandomValues(bytes);
    return Array.from(bytes)
      .map((value) => value.toString(16).padStart(2, '0'))
      .join('');
  }
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}

/**
 * 读取已保存的 OAuth2 state。
 *
 * @param storage 会话存储
 * @returns 已保存 state；格式错误时返回空
 */
function readStoredState(storage: AdminOAuthStorage): { state: string; returnTo: string; codeVerifier: string } | null {
  const rawState = storage.getItem(ADMIN_OAUTH_STATE_STORAGE_KEY);
  if (!rawState) {
    return null;
  }
  try {
    const parsedState = JSON.parse(rawState) as Partial<{ state: string; returnTo: string; codeVerifier: string }>;
    const state = normalizeText(parsedState.state);
    const codeVerifier = normalizeText(parsedState.codeVerifier);
    if (!state || !codeVerifier) {
      return null;
    }
    return {
      state,
      codeVerifier,
      returnTo: normalizeReturnTo(parsedState.returnTo),
    };
  } catch {
    return null;
  }
}

/**
 * 创建 PKCE code_verifier。
 *
 * @returns 符合 RFC 7636 长度要求的随机校验码
 */
function createPkceCodeVerifier(): string {
  if (typeof crypto === 'undefined' || typeof crypto.getRandomValues !== 'function') {
    throw new Error('当前浏览器不支持安全随机数，无法发起统一认证');
  }
  const bytes = new Uint8Array(32);
  crypto.getRandomValues(bytes);
  return base64UrlEncode(bytes);
}

/**
 * 基于 code_verifier 创建 S256 code_challenge。
 *
 * @param codeVerifier PKCE 原始校验码
 * @returns 可提交给认证中心的 code_challenge
 */
async function createPkceCodeChallenge(codeVerifier: string): Promise<string> {
  if (typeof crypto === 'undefined' || !crypto.subtle || typeof crypto.subtle.digest !== 'function') {
    throw new Error('当前浏览器不支持 PKCE S256，无法发起统一认证');
  }
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(codeVerifier));
  return base64UrlEncode(new Uint8Array(digest));
}

/**
 * 转换为 OAuth2 PKCE 使用的 Base64URL 文本。
 *
 * @param bytes 原始字节
 * @returns 去除填充符的 Base64URL 文本
 */
function base64UrlEncode(bytes: Uint8Array): string {
  const binary = Array.from(bytes)
    .map((value) => String.fromCharCode(value))
    .join('');
  return btoa(binary)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/u, '');
}

/**
 * 标准化后台返回路径，避免外部跳转地址进入认证流程。
 *
 * @param value 原始返回路径
 * @returns 后台内部相对路径
 */
function normalizeReturnTo(value: unknown): string {
  const normalizedValue = normalizeText(value);
  return normalizedValue.startsWith('/') && !normalizedValue.startsWith('//') ? normalizedValue : '/dashboard';
}

/**
 * 校验必填文本。
 *
 * @param value 原始文本
 * @param message 文本为空时的错误消息
 * @returns 去除首尾空白后的文本
 */
function requireText(value: unknown, message: string): string {
  const normalizedValue = normalizeText(value);
  if (!normalizedValue) {
    throw new Error(message);
  }
  return normalizedValue;
}

/**
 * 标准化文本。
 *
 * @param value 原始文本
 * @returns 去除首尾空白后的文本
 */
function normalizeText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}
