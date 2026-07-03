/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { parsePlatformTokenClaims } from './platformToken';

/**
 * 移动端运行时信息。
 */
export interface MobilePlatformRuntime {
  /** uni-app 运行平台。 */
  platform: string;
  /** 是否 H5 环境。 */
  h5: boolean;
  /** 是否小程序环境。 */
  miniProgram: boolean;
  /** 是否 App 环境。 */
  app: boolean;
  /** 当前窗口宽度。 */
  windowWidth: number;
}

/**
 * 移动端用户上下文。
 */
export interface MobileUserContext {
  /** 当前用户 ID。 */
  userId: number | null;
  /** 当前组织 ID。 */
  orgId: number | null;
  /** 当前账号名称。 */
  accountName: string;
  /** 当前租户编码。 */
  tenantId: string;
  /** 当前角色名称。 */
  roleName: string;
  /** 当前访问令牌。 */
  accessToken?: string;
  /** 是否已经登录。 */
  loggedIn: boolean;
}

/** 移动端用户上下文本地缓存键。 */
const USER_CONTEXT_STORAGE_KEY = 'ZHYC_MOBILE_USER_CONTEXT';
/** 移动端登录页路径。 */
const MOBILE_LOGIN_PAGE_PATH = '/pages/auth/login';
/** 历史登录页兼容路径。 */
const MOBILE_LEGACY_LOGIN_PAGE_PATH = '/pages/login/index';
/** 未登录状态允许访问的公开页面路径。 */
const MOBILE_PUBLIC_PAGE_PATHS = new Set([
  MOBILE_LOGIN_PAGE_PATH,
  MOBILE_LEGACY_LOGIN_PAGE_PATH,
]);
/** 移动端底部 Tab 页面路径集合。 */
const MOBILE_TAB_PAGE_PATHS = new Set([
  '/pages/workbench/index',
  '/pages/workflow/todo',
  '/pages/ai/index',
  '/pages/profile/index',
]);

/** 移动端登录上下文错误关键字。 */
const MOBILE_CONTEXT_ERROR_KEYWORDS = [
  '请先登录移动端账号',
  '移动端用户 ID 缺失',
  '移动端组织 ID 缺失',
  '移动端租户编码缺失',
  '移动端访问令牌缺失',
];

/**
 * 判断错误是否属于移动端登录上下文问题。
 *
 * @param message 错误文案
 * @returns 是否为未登录、租户缺失、用户缺失或令牌缺失
 */
export function isMobileContextErrorMessage(message: unknown): boolean {
  if (typeof message !== 'string' || !message.trim()) {
    return false;
  }
  return MOBILE_CONTEXT_ERROR_KEYWORDS.some((keyword) => message.includes(keyword));
}

/**
 * 判断错误是否属于服务不可用或网络不可达。
 *
 * @param message 错误文案
 * @returns 是否为后端服务、网络或接口发布状态异常
 */
export function isMobileServiceErrorMessage(message: unknown): boolean {
  if (typeof message !== 'string' || !message.trim()) {
    return false;
  }
  return message.includes('服务暂不可用')
    || message.includes('核心平台服务')
    || message.includes('网络连接失败')
    || message.includes('服务响应超时')
    || message.includes('网络请求超时')
    || message.includes('接口暂未开放');
}

/**
 * 标准化移动端错误提示。
 *
 * @param error 原始异常
 * @param fallbackMessage 兜底文案
 * @returns 可直接展示的错误文案
 */
export function normalizeMobileErrorMessage(error: unknown, fallbackMessage: string): string {
  return error instanceof Error && error.message ? error.message : fallbackMessage;
}

/**
 * 获取移动端运行时信息。
 *
 * @returns 当前运行平台和屏幕信息
 */
export function getMobilePlatformRuntime(): MobilePlatformRuntime {
  const systemInfo = uni.getSystemInfoSync();
  const uniPlatform = (systemInfo.uniPlatform || systemInfo.platform || 'unknown').toLowerCase();
  return {
    platform: uniPlatform,
    h5: uniPlatform === 'web',
    miniProgram: uniPlatform.includes('mp'),
    app: uniPlatform === 'app',
    windowWidth: systemInfo.windowWidth,
  };
}

/**
 * 获取移动端用户上下文。
 *
 * <p>首期从 uni storage 读取登录上下文，避免页面写死演示账号；没有登录信息时返回明确的未登录状态。</p>
 *
 * @returns 移动端用户上下文
 */
export function getMobileUserContext(): MobileUserContext {
  const storedContext = uni.getStorageSync(USER_CONTEXT_STORAGE_KEY) as Partial<MobileUserContext> | '';
  if (!storedContext || typeof storedContext !== 'object') {
    return buildAnonymousUserContext();
  }
  return {
    userId: normalizeMobileUserId(storedContext.userId),
    orgId: normalizeMobileOrgId(storedContext.orgId),
    accountName: normalizeMobileText(storedContext.accountName) || '未登录',
    tenantId: normalizeMobileText(storedContext.tenantId),
    roleName: normalizeMobileText(storedContext.roleName) || '未分配角色',
    accessToken: normalizeMobileText(storedContext.accessToken) || undefined,
    loggedIn: Boolean(storedContext.loggedIn),
  };
}

/**
 * 保存移动端用户上下文。
 *
 * @param context 移动端用户上下文
 */
export function saveMobileUserContext(context: MobileUserContext): void {
  const tokenClaims = context.accessToken ? parsePlatformTokenClaims(context.accessToken) : null;
  uni.setStorageSync(USER_CONTEXT_STORAGE_KEY, {
    ...context,
    tenantId: tokenClaims?.tenantId ?? context.tenantId,
    userId: tokenClaims?.userId ?? context.userId,
    accountName: tokenClaims?.accountName ?? context.accountName,
  });
}

/**
 * 清理移动端用户上下文。
 */
export function clearMobileUserContext(): void {
  uni.removeStorageSync(USER_CONTEXT_STORAGE_KEY);
}

/**
 * 跳转移动端登录页。
 *
 * <p>业务请求发现未登录、令牌无效或认证上下文缺失时统一进入该入口，避免页面各自散落登录判断。</p>
 *
 * @param reason 登录拦截原因
 * @param returnTo 登录后返回页面
 */
export function redirectToMobileLogin(reason = '请先登录移动端账号', returnTo = resolveCurrentMobilePagePath()): void {
  if (isMobilePublicPage(returnTo)) {
    return;
  }
  const loginUrl = `${MOBILE_LOGIN_PAGE_PATH}?returnTo=${encodeURIComponent(returnTo)}&reason=${encodeURIComponent(reason)}`;
  uni.reLaunch({ url: loginUrl });
}

/**
 * 校验当前移动端页面是否允许未登录访问。
 *
 * <p>除统一登录页和历史登录兼容页外，移动端功能页必须先具备登录上下文，避免未登录用户看到业务功能结构。</p>
 *
 * @param reason 登录拦截原因
 * @param returnTo 登录后返回页面
 * @returns 当前页面是否可继续渲染业务能力
 */
export function guardMobileAuthenticatedPage(
  reason = '请先登录移动端账号',
  returnTo = resolveCurrentMobilePagePath(),
): boolean {
  if (isMobilePublicPage(returnTo)) {
    return true;
  }
  const userContext = getMobileUserContext();
  if (userContext.loggedIn) {
    return true;
  }
  redirectToMobileLogin(reason, returnTo);
  return false;
}

/**
 * 登录成功后返回目标页面。
 *
 * @param returnTo 登录前页面路径
 */
export function redirectAfterMobileLogin(returnTo: string): void {
  const normalizedReturnTo = normalizeMobilePagePath(returnTo) || '/pages/workbench/index';
  if (MOBILE_TAB_PAGE_PATHS.has(normalizedReturnTo.split('?')[0])) {
    uni.switchTab({ url: normalizedReturnTo.split('?')[0] });
    return;
  }
  uni.reLaunch({ url: normalizedReturnTo });
}

/**
 * 打开移动端站内页面。
 *
 * <p>uni-app 的 TabBar 页面必须使用 {@code switchTab}，普通页面使用 {@code navigateTo}，统一封装避免入口点击失效。</p>
 *
 * @param url 站内页面路径
 */
export function openMobilePage(url: string): void {
  const normalizedUrl = normalizeMobilePagePath(url);
  if (!normalizedUrl) {
    showMobileToast('页面地址无效', 'none');
    return;
  }
  const pagePath = normalizedUrl.split('?')[0];
  if (MOBILE_TAB_PAGE_PATHS.has(pagePath)) {
    uni.switchTab({ url: pagePath });
    return;
  }
  uni.navigateTo({ url: normalizedUrl });
}

/**
 * 获取当前已登录用户 ID。
 *
 * @param context 已读取的移动端用户上下文
 * @returns 当前用户 ID
 */
export function requireMobileUserId(context: MobileUserContext = getMobileUserContext()): number {
  if (!context.loggedIn) {
    throw new Error('请先登录移动端账号');
  }
  if (context.userId === null || !Number.isFinite(context.userId)) {
    throw new Error('移动端用户 ID 缺失');
  }
  return context.userId;
}

/**
 * 获取当前移动端组织 ID。
 *
 * @param context 已读取的移动端用户上下文
 * @returns 当前组织 ID
 */
export function requireMobileOrgId(context: MobileUserContext = getMobileUserContext()): number {
  if (!context.loggedIn) {
    throw new Error('请先登录移动端账号');
  }
  if (context.orgId === null || !Number.isFinite(context.orgId)) {
    throw new Error('移动端组织 ID 缺失');
  }
  return context.orgId;
}

/**
 * 获取当前登录租户编码。
 *
 * @param context 已读取的移动端用户上下文
 * @returns 当前租户编码
 */
export function requireMobileTenantId(context: MobileUserContext = getMobileUserContext()): string {
  if (!context.loggedIn) {
    throw new Error('请先登录移动端账号');
  }
  if (!context.tenantId) {
    throw new Error('移动端租户编码缺失');
  }
  return context.tenantId;
}

/**
 * 构建匿名用户上下文。
 *
 * @returns 匿名用户上下文
 */
function buildAnonymousUserContext(): MobileUserContext {
  return {
    userId: null,
    orgId: null,
    accountName: '未登录',
    tenantId: '',
    roleName: '未分配角色',
    accessToken: undefined,
    loggedIn: false,
  };
}

/**
 * 标准化移动端文本。
 *
 * @param value 原始文本
 * @returns 去除首尾空白后的文本
 */
function normalizeMobileText(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

/**
 * 标准化移动端用户 ID。
 *
 * @param value 原始用户 ID
 * @returns 有效用户 ID 或 {@code null}
 */
function normalizeMobileUserId(value: unknown): number | null {
  const parsedUserId = Number(value);
  return Number.isFinite(parsedUserId) && parsedUserId > 0 ? parsedUserId : null;
}

/**
 * 标准化移动端组织 ID。
 *
 * @param value 原始组织 ID
 * @returns 有效组织 ID 或 {@code null}
 */
function normalizeMobileOrgId(value: unknown): number | null {
  const parsedOrgId = Number(value);
  return Number.isFinite(parsedOrgId) && parsedOrgId > 0 ? parsedOrgId : null;
}

/**
 * 跨端二次确认弹窗。
 *
 * @param title 弹窗标题
 * @param content 弹窗内容
 * @returns 用户是否确认
 */
export function showConfirm(title: string, content: string): Promise<boolean> {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      confirmText: '确认',
      cancelText: '取消',
      success: (result) => {
        resolve(Boolean(result.confirm));
      },
      fail: () => {
        resolve(false);
      },
    });
  });
}

/**
 * 展示跨端轻提示。
 *
 * @param title 提示文案
 * @param icon 提示图标
 */
export function showMobileToast(title: string, icon: 'success' | 'error' | 'none' = 'none'): void {
  uni.showToast({
    title,
    icon,
  });
}

/**
 * 解析当前移动端页面路径。
 *
 * @returns 当前页面路径，无法解析时返回首页
 */
function resolveCurrentMobilePagePath(): string {
  const h5HashPagePath = resolveH5HashPagePath();
  if (h5HashPagePath) {
    return h5HashPagePath;
  }
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
  const currentPage = pages.length ? pages[pages.length - 1] : null;
  if (!currentPage?.route) {
    return '/pages/workbench/index';
  }
  const pagePath = `/${currentPage.route}`;
  const query = encodePageOptions(currentPage.options);
  return `${pagePath}${query ? `?${query}` : ''}`;
}

/**
 * 从 H5 hash 中解析当前页面。
 *
 * <p>H5 首屏加载时 {@code getCurrentPages()} 可能短暂保留入口页，优先读取地址栏可避免登录页被误判为受保护页面。</p>
 *
 * @returns H5 当前页面路径，非 H5 或无法解析时返回空字符串
 */
function resolveH5HashPagePath(): string {
  if (typeof location === 'undefined') {
    return '';
  }
  const hashValue = location.hash || '';
  if (!hashValue.startsWith('#/')) {
    return '';
  }
  const routeValue = hashValue.slice(1);
  if (routeValue === '/') {
    return '/pages/workbench/index';
  }
  return routeValue.startsWith('/pages/') ? routeValue : '';
}

/**
 * 编码页面参数。
 *
 * @param options 页面参数
 * @returns 查询字符串
 */
function encodePageOptions(options: Record<string, unknown> | undefined): string {
  if (!options) {
    return '';
  }
  return Object.entries(options)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&');
}

/**
 * 标准化移动页面路径。
 *
 * @param value 原始页面路径
 * @returns 安全的站内页面路径
 */
function normalizeMobilePagePath(value: unknown): string {
  if (typeof value !== 'string') {
    return '';
  }
  const trimmedValue = value.trim();
  return trimmedValue.startsWith('/pages/') ? trimmedValue : '';
}

/**
 * 判断移动端页面是否为公开页面。
 *
 * @param pagePath 页面路径，可携带查询参数
 * @returns 是否允许未登录访问
 */
function isMobilePublicPage(pagePath: string): boolean {
  const normalizedPath = pagePath.split('?')[0];
  return MOBILE_PUBLIC_PAGE_PATHS.has(normalizedPath);
}
