/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { refreshAdminOAuthToken } from '@/api/auth/oauth';
import {
  clearAdminRuntimeContext,
  getAdminRuntimeContext,
  saveAdminRuntimeContext,
  type AdminRuntimeContext,
} from '@/utils/adminContext';

/**
 * 后端统一响应结构。
 */
export interface ApiResult<T> {
  /** 是否成功。 */
  success: boolean;
  /** 业务响应码。 */
  code: string;
  /** 响应消息。 */
  message: string;
  /** 响应数据。 */
  data: T;
  /** 服务端响应时间戳。 */
  timestamp?: number;
}

/**
 * 后台管理端统一分页响应结构。
 */
export interface PageResult<T> {
  /** 总记录数。 */
  total: number;
  /** 当前页码，从 1 开始。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
  /** 当前页记录。 */
  records: T[];
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

/**
 * HTTP 请求参数。
 */
export interface HttpRequestOptions<TBody = unknown> {
  /** 请求方法。 */
  method?: HttpMethod;
  /** 请求体。 */
  body?: TBody;
  /** 查询参数。 */
  query?: Record<string, string | number | boolean | undefined>;
  /** 是否匿名访问；公开页面接口使用，跳过后台租户、用户和令牌请求头。 */
  anonymous?: boolean;
  /** 额外请求头；业务 API 不得在这里传租户、用户、鉴权或签名类安全上下文。 */
  headers?: Record<string, string>;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
/** 访问令牌临期刷新提前量，单位毫秒。 */
const ACCESS_TOKEN_REFRESH_WINDOW_MS = 60_000;
/** 正在执行的访问令牌刷新任务；用于合并同一页面内的并发业务请求。 */
let accessTokenRefreshPromise: Promise<void> | null = null;

/**
 * 发送后台管理端请求。
 *
 * @param path 后端接口路径
 * @param options 请求配置
 * @returns 解包后的业务数据
 */
export async function request<TResponse, TBody = unknown>(
  path: string,
  options: HttpRequestOptions<TBody> = {},
): Promise<TResponse> {
  if (!options.anonymous) {
    await refreshAccessTokenIfNeeded();
  }
  const url = new URL(`${apiBaseUrl}${path}`, window.location.origin);
  Object.entries(options.query ?? {}).forEach(([key, value]) => {
    if (value !== undefined) {
      url.searchParams.set(key, String(value));
    }
  });

  const isFormDataBody = options.body instanceof FormData;
  const response = await fetch(url, {
    method: options.method ?? 'GET',
    headers: {
      ...(isFormDataBody ? {} : { 'Content-Type': 'application/json' }),
      ...options.headers,
      ...(options.anonymous ? {} : buildAdminRequestHeaders()),
    },
    body: resolveRequestBody(options.body, isFormDataBody),
  });

  if (!response.ok) {
    if (response.status === 401) {
      redirectToLoginAfterUnauthorized();
      throw new Error('后台登录态已失效，请重新登录');
    }
    throw new Error(await resolveHttpErrorMessage(response, '接口请求失败'));
  }

  const result = (await response.json()) as ApiResult<TResponse>;
  if (!result.success) {
    throw new Error(result.message || result.code || '业务处理失败');
  }
  return result.data;
}

/**
 * 根据请求体类型解析 fetch body。
 *
 * @param body 原始请求体
 * @param isFormDataBody 是否为文件表单体
 * @returns 可直接传给 fetch 的请求体
 */
function resolveRequestBody(body: unknown, isFormDataBody: boolean): BodyInit | undefined {
  if (body === undefined) {
    return undefined;
  }
  return isFormDataBody ? (body as FormData) : JSON.stringify(body);
}

/**
 * 构建后台管理接口基础安全请求头。
 *
 * @returns 已通过登录态、租户、用户和令牌校验的请求头
 */
export function buildAdminRequestHeaders(): Record<string, string> {
  const context = requireAuthenticatedAdminContext();
  return {
    'X-ZHYC-Tenant-Id': context.tenantId,
    'X-ZHYC-User-Id': String(context.userId),
    Authorization: `Bearer ${context.accessToken}`,
  };
}

/**
 * 获取已认证的后台管理端运行时上下文。
 *
 * <p>后台管理接口必须同时具备用户、租户和访问令牌，避免空租户访问或未授权请求进入后端。</p>
 *
 * @returns 已认证且带访问令牌的后台上下文
 */
export function requireAuthenticatedAdminContext(): AdminRuntimeContext & { userId: number; accessToken: string } {
  const context = getAdminRuntimeContext();
  if (context.userId === null || !Number.isFinite(context.userId)) {
    throw new Error('请先登录后台管理账号');
  }
  if (!context.tenantId) {
    throw new Error('请先选择后台管理租户');
  }
  if (!context.accessToken) {
    throw new Error('后台管理访问令牌缺失');
  }
  return {
    tenantId: context.tenantId,
    userId: context.userId,
    orgId: context.orgId,
    accountName: context.accountName,
    accessToken: context.accessToken,
  };
}

/**
 * 在业务请求前按需刷新访问令牌。
 *
 * <p>只在本地记录的过期时间进入刷新窗口且存在刷新令牌时触发，避免每次请求都访问认证中心。</p>
 */
async function refreshAccessTokenIfNeeded(): Promise<void> {
  const context = getAdminRuntimeContext();
  if (!shouldRefreshAccessToken(context)) {
    return;
  }
  const refreshToken = context.refreshToken;
  if (!refreshToken) {
    return;
  }
  if (!accessTokenRefreshPromise) {
    accessTokenRefreshPromise = doRefreshAccessToken(context, refreshToken)
      .finally(() => {
        accessTokenRefreshPromise = null;
      });
  }
  await accessTokenRefreshPromise;
}

/**
 * 执行访问令牌刷新。
 *
 * <p>由 {@link refreshAccessTokenIfNeeded} 统一合并并发请求，避免安全中心等页面同时发起多个刷新请求。</p>
 *
 * @param context 当前后台运行时上下文
 * @param refreshToken 已存在的刷新令牌
 */
async function doRefreshAccessToken(context: AdminRuntimeContext, refreshToken: string): Promise<void> {
  try {
    const tokenResponse = await refreshAdminOAuthToken({ refreshToken });
    saveAdminRuntimeContext({
      ...context,
      accessToken: tokenResponse.accessToken,
      refreshToken: tokenResponse.refreshToken || refreshToken,
      accessTokenExpiresAt: calculateAccessTokenExpiresAt(tokenResponse.expiresIn),
    });
  } catch {
    redirectToLoginAfterUnauthorized();
    throw new Error('后台登录态刷新失败，请重新登录');
  }
}

/**
 * 解析 HTTP 非 2xx 响应中的业务错误消息。
 *
 * @param response fetch 响应
 * @param fallback 默认错误前缀
 * @returns 用户可读错误消息
 */
async function resolveHttpErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as Partial<ApiResult<unknown>>;
    if (typeof result.message === 'string' && result.message.trim()) {
      return result.message.trim();
    }
    if (typeof result.code === 'string' && result.code.trim()) {
      return result.code.trim();
    }
  } catch {
    // 非 JSON 响应保持状态码提示，避免把 HTML 错误页展示给用户。
  }
  return `${fallback}: ${response.status}`;
}

/**
 * 判断访问令牌是否需要刷新。
 *
 * @param context 当前后台运行时上下文
 * @returns 访问令牌已过期或即将过期时返回 true
 */
function shouldRefreshAccessToken(context: AdminRuntimeContext): boolean {
  return Boolean(context.accessToken
    && context.refreshToken
    && context.accessTokenExpiresAt
    && context.accessTokenExpiresAt <= Date.now() + ACCESS_TOKEN_REFRESH_WINDOW_MS);
}

/**
 * 计算访问令牌过期时间。
 *
 * @param expiresIn 访问令牌有效期秒数
 * @returns 访问令牌过期时间戳，无法判断时返回 null
 */
function calculateAccessTokenExpiresAt(expiresIn?: number): number | null {
  return typeof expiresIn === 'number' && Number.isFinite(expiresIn) && expiresIn > 0
    ? Date.now() + expiresIn * 1000
    : null;
}

/**
 * 处理后台访问令牌失效。
 *
 * <p>清理本地运行时上下文，并带上当前页面路径跳转统一认证登录页，便于登录后回到原业务页面。</p>
 */
function redirectToLoginAfterUnauthorized(): void {
  clearAdminRuntimeContext();
  const returnTo = `${window.location.pathname}${window.location.search}`;
  window.location.href = `${window.location.origin}/login?returnTo=${encodeURIComponent(returnTo)}`;
}
