/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 后台管理端运行时上下文。
 */
export interface AdminRuntimeContext {
  /** 当前租户编码；用于后台管理接口共享表模式下的数据隔离。 */
  tenantId: string;
  /** 当前用户 ID；为空时表示后台管理账号尚未登录。 */
  userId: number | null;
  /** 当前组织 ID；用于采购、岗位等组织归属类业务数据。 */
  orgId: number | null;
  /** 当前账号名称；用于页面展示当前操作主体。 */
  accountName: string;
  /** 当前访问令牌；用于后台管理接口 Bearer Token 鉴权。 */
  accessToken?: string;
  /** 当前刷新令牌；用于访问令牌过期前通过核心平台 BFF 静默续期。 */
  refreshToken?: string;
  /** 当前访问令牌过期时间戳，毫秒；为空时表示无法判断过期时间。 */
  accessTokenExpiresAt?: number | null;
}

/** 后台管理端上下文本地缓存键。 */
const ADMIN_CONTEXT_STORAGE_KEY = 'ZHYC_ADMIN_RUNTIME_CONTEXT';
/** 后台管理端运行时上下文变更事件名，用于同一浏览器标签页内同步认证状态。 */
const ADMIN_CONTEXT_CHANGED_EVENT = 'zhyc-admin-runtime-context-changed';

/**
 * 后台上下文缓存最小存储接口。
 */
interface AdminContextStorage {
  /** 按键读取缓存值。 */
  getItem(key: string): string | null;
  /** 按键写入缓存值。 */
  setItem(key: string, value: string): void;
  /** 按键移除缓存值。 */
  removeItem(key: string): void;
}

/**
 * 后台上下文变更监听函数。
 */
type AdminContextChangeListener = (context: AdminRuntimeContext) => void;

/**
 * 获取后台管理端运行时上下文。
 *
 * @returns 当前后台上下文；未登录或未选择租户时返回空上下文
 */
export function getAdminRuntimeContext(): AdminRuntimeContext {
  const storedContext = readStoredContext();
  return {
    tenantId: normalizeText(storedContext.tenantId),
    userId: normalizeUserId(storedContext.userId),
    orgId: normalizeOrgId(storedContext.orgId),
    accountName: normalizeText(storedContext.accountName) || '未登录',
    accessToken: normalizeText(storedContext.accessToken) || undefined,
    refreshToken: normalizeText(storedContext.refreshToken) || undefined,
    accessTokenExpiresAt: normalizeExpiresAt(storedContext.accessTokenExpiresAt),
  };
}

/**
 * 判断后台管理端是否具备可访问业务页面的登录态。
 *
 * <p>业务页面必须同时具备访问令牌、租户和用户，避免未登录用户先看到菜单、页面内容或本地缓存数据。</p>
 *
 * @param context 已读取的后台上下文，默认从本地缓存读取
 * @returns 已具备后台业务页面访问上下文时返回 true
 */
export function hasAuthenticatedAdminContext(context: AdminRuntimeContext = getAdminRuntimeContext()): boolean {
  return Boolean(
    context.accessToken
      && context.tenantId
      && context.userId !== null
      && Number.isFinite(context.userId),
  );
}

/**
 * 获取当前租户编码。
 *
 * @param context 已读取的后台上下文
 * @returns 当前租户编码
 */
export function requireAdminTenantId(context: AdminRuntimeContext = getAdminRuntimeContext()): string {
  if (!context.tenantId) {
    throw new Error('请先选择后台管理租户');
  }
  return context.tenantId;
}

/**
 * 获取当前登录用户 ID。
 *
 * @param context 已读取的后台上下文
 * @returns 当前登录用户 ID
 */
export function requireAdminUserId(context: AdminRuntimeContext = getAdminRuntimeContext()): number {
  if (context.userId === null || !Number.isFinite(context.userId)) {
    throw new Error('请先登录后台管理账号');
  }
  return context.userId;
}

/**
 * 获取当前组织 ID。
 *
 * @param context 已读取的后台上下文
 * @returns 当前组织 ID
 */
export function requireAdminOrgId(context: AdminRuntimeContext = getAdminRuntimeContext()): number {
  if (context.orgId === null || !Number.isFinite(context.orgId)) {
    throw new Error('请先选择当前组织');
  }
  return context.orgId;
}

/**
 * 保存后台管理端运行时上下文。
 *
 * @param context 后台管理端运行时上下文
 */
export function saveAdminRuntimeContext(context: AdminRuntimeContext): void {
  window.localStorage.setItem(ADMIN_CONTEXT_STORAGE_KEY, JSON.stringify(context));
  notifyAdminRuntimeContextChanged();
}

/**
 * 清理后台管理端运行时上下文。
 *
 * <p>用于用户主动退出或访问令牌失效时移除本地租户、用户和访问令牌，避免继续携带过期身份访问业务接口。</p>
 *
 * @param storage 后台上下文缓存，默认使用浏览器 localStorage
 */
export function clearAdminRuntimeContext(storage: AdminContextStorage = window.localStorage): void {
  storage.removeItem(ADMIN_CONTEXT_STORAGE_KEY);
  notifyAdminRuntimeContextChanged();
}

/**
 * 订阅后台运行时上下文变更。
 *
 * <p>浏览器同标签页内写入 localStorage 不会触发原生 storage 事件，因此统一认证回调、手动运行时保存、
 * 静默刷新和退出登录都需要通过本事件主动同步顶栏认证状态。</p>
 *
 * @param listener 上下文变更后的处理函数
 * @returns 取消订阅函数
 */
export function subscribeAdminRuntimeContextChange(listener: AdminContextChangeListener): () => void {
  const handleContextChanged = (): void => {
    listener(getAdminRuntimeContext());
  };
  const handleStorageChanged = (event: StorageEvent): void => {
    if (event.key === ADMIN_CONTEXT_STORAGE_KEY) {
      handleContextChanged();
    }
  };
  window.addEventListener(ADMIN_CONTEXT_CHANGED_EVENT, handleContextChanged);
  window.addEventListener('storage', handleStorageChanged);
  return () => {
    window.removeEventListener(ADMIN_CONTEXT_CHANGED_EVENT, handleContextChanged);
    window.removeEventListener('storage', handleStorageChanged);
  };
}

/**
 * 读取本地缓存中的后台上下文。
 *
 * @returns 本地上下文片段
 */
function readStoredContext(): Partial<AdminRuntimeContext> {
  const rawContext = window.localStorage.getItem(ADMIN_CONTEXT_STORAGE_KEY);
  if (!rawContext) {
    return {};
  }
  try {
    const parsedContext = JSON.parse(rawContext) as Partial<AdminRuntimeContext>;
    return parsedContext && typeof parsedContext === 'object' ? parsedContext : {};
  } catch {
    return {};
  }
}

/**
 * 通知当前页面后台运行时上下文已变更。
 */
function notifyAdminRuntimeContextChanged(): void {
  window.dispatchEvent(new CustomEvent(ADMIN_CONTEXT_CHANGED_EVENT));
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

/**
 * 标准化用户 ID。
 *
 * @param value 原始用户 ID
 * @returns 有效用户 ID 或 null
 */
function normalizeUserId(value: unknown): number | null {
  const parsedUserId = Number(value);
  return Number.isFinite(parsedUserId) && parsedUserId > 0 ? parsedUserId : null;
}

/**
 * 标准化组织 ID。
 *
 * @param value 原始组织 ID
 * @returns 有效组织 ID 或 null
 */
function normalizeOrgId(value: unknown): number | null {
  const parsedOrgId = Number(value);
  return Number.isFinite(parsedOrgId) && parsedOrgId > 0 ? parsedOrgId : null;
}

/**
 * 标准化访问令牌过期时间。
 *
 * @param value 原始过期时间戳
 * @returns 有效过期时间戳或 null
 */
function normalizeExpiresAt(value: unknown): number | null {
  const parsedExpiresAt = Number(value);
  return Number.isFinite(parsedExpiresAt) && parsedExpiresAt > 0 ? parsedExpiresAt : null;
}
