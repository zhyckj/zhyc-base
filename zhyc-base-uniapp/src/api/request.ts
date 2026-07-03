/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { getMobilePlatformApiBaseUrl } from '@/config/runtime';
import {
  clearMobileUserContext,
  getMobileUserContext,
  redirectToMobileLogin,
  type MobileUserContext,
} from '@/utils/platform';

/**
 * 移动端统一响应结构。
 */
export interface MobileApiResult<T> {
  /** 是否成功。 */
  success: boolean;
  /** 业务响应码。 */
  code: string;
  /** 响应消息。 */
  message: string;
  /** 响应数据。 */
  data: T;
}

/**
 * 移动端统一分页响应结构。
 */
export interface MobilePageResult<T> {
  /** 总记录数。 */
  total: number;
  /** 当前页码，从 1 开始。 */
  pageNo: number;
  /** 每页记录数。 */
  pageSize: number;
  /** 当前页记录。 */
  records: T[];
}

export type MobileHttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

/**
 * 移动端请求参数。
 */
export interface MobileRequestOptions<TBody = unknown> {
  /** 请求方法。 */
  method?: MobileHttpMethod;
  /** 接口基地址；不传时使用核心平台 API 基地址。 */
  baseUrl?: string;
  /** 是否携带移动端登录态；登录、换令牌等接口必须显式传 false。 */
  auth?: boolean;
  /** 请求体。 */
  data?: TBody;
  /** 查询参数。 */
  query?: Record<string, string | number | boolean | undefined>;
  /** 额外请求头；业务 API 不得在这里传租户、用户、鉴权或签名类安全上下文。 */
  headers?: Record<string, string>;
}

/**
 * 发送移动端请求。
 *
 * @param path 后端接口路径
 * @param options 请求参数
 * @returns 解包后的业务数据
 */
export function mobileRequest<TResponse, TBody = unknown>(
  path: string,
  options: MobileRequestOptions<TBody> = {},
): Promise<TResponse> {
  const query = Object.entries(options.query ?? {})
    .filter(([, value]) => value !== undefined)
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&');
  const requestBaseUrl = (options.baseUrl || getMobilePlatformApiBaseUrl()).replace(/\/+$/, '');
  const url = `${requestBaseUrl}${path}${query ? `?${query}` : ''}`;
  let requestHeaders: Record<string, string>;
  try {
    requestHeaders = {
      'Content-Type': 'application/json',
      ...options.headers,
      ...(options.auth === false ? {} : buildMobileRequestHeaders()),
    };
  } catch (error) {
    const message = error instanceof Error ? error.message : '请先登录移动端账号';
    redirectToMobileLogin(message);
    return Promise.reject(error);
  }

  return new Promise<TResponse>((resolve, reject) => {
    uni.request<MobileApiResult<TResponse>>({
      url,
      method: options.method ?? 'GET',
      data: options.data,
      header: requestHeaders,
      success: (result) => {
        if (result.statusCode === 401 || result.statusCode === 403) {
          const message = resolveMobileErrorMessage(result.data) || '登录状态已失效，请重新登录';
          clearMobileUserContext();
          redirectToMobileLogin(message);
          reject(new Error(message));
          return;
        }
        if (result.statusCode < 200 || result.statusCode >= 300) {
          reject(new Error(resolveMobileHttpErrorMessage(result.statusCode)));
          return;
        }
        if (!result.data || typeof result.data !== 'object') {
          reject(new Error('接口响应格式不正确'));
          return;
        }
        if (!result.data.success) {
          const message = result.data.message || result.data.code || '业务处理失败';
          if (isMobileAuthFailure(result.data.code, message)) {
            clearMobileUserContext();
            redirectToMobileLogin(message);
          }
          reject(new Error(message));
          return;
        }
        resolve(result.data.data);
      },
      fail: (error) => {
        reject(new Error(resolveMobileNetworkErrorMessage(error.errMsg)));
      },
    });
  });
}

/**
 * 解析移动端 HTTP 层错误文案。
 *
 * @param statusCode HTTP 状态码
 * @returns 面向移动端用户的错误文案
 */
function resolveMobileHttpErrorMessage(statusCode: number): string {
  if (statusCode === 404) {
    return '接口暂未开放，请确认移动端后端接口已发布';
  }
  if (statusCode === 408 || statusCode === 504) {
    return '服务响应超时，请稍后重试';
  }
  if (statusCode >= 500) {
    return '服务暂不可用，请确认核心平台服务已启动后重试';
  }
  return `请求处理失败，请稍后重试（${statusCode}）`;
}

/**
 * 解析移动端网络层错误文案。
 *
 * @param errMsg uni.request 原始错误
 * @returns 面向移动端用户的错误文案
 */
function resolveMobileNetworkErrorMessage(errMsg: string | undefined): string {
  if (!errMsg) {
    return '网络连接失败，请确认核心平台服务可访问';
  }
  if (errMsg.includes('timeout')) {
    return '网络请求超时，请稍后重试';
  }
  if (errMsg.includes('fail') || errMsg.includes('abort') || errMsg.includes('refused')) {
    return '网络连接失败，请确认核心平台服务可访问';
  }
  return errMsg;
}

/**
 * 解析移动端错误响应文案。
 *
 * @param data 接口响应体
 * @returns 错误文案
 */
function resolveMobileErrorMessage(data: unknown): string {
  if (!data || typeof data !== 'object') {
    return '';
  }
  const result = data as Partial<MobileApiResult<unknown>>;
  return typeof result.message === 'string' ? result.message : '';
}

/**
 * 判断业务响应是否属于登录或权限拦截。
 *
 * @param code 业务响应码
 * @param message 响应消息
 * @returns 是否需要进入登录页
 */
function isMobileAuthFailure(code: string | undefined, message: string): boolean {
  return code === 'PERMISSION_DENIED'
    || code === 'AUTH_CENTER_TOKEN_INVALID'
    || message.includes('未登录')
    || message.includes('没有权限')
    || message.includes('登录状态已失效');
}

/**
 * 构建移动端基础请求头。
 *
 * <p>租户、用户和令牌来自移动端登录上下文；未登录场景不写入默认租户，避免跨租户误用。</p>
 *
 * @returns 移动端基础请求头
 */
export function buildMobileRequestHeaders(): Record<string, string> {
  const userContext = requireAuthenticatedMobileContext();
  return {
    'X-ZHYC-Tenant-Id': userContext.tenantId,
    'X-ZHYC-User-Id': String(userContext.userId),
    Authorization: `Bearer ${userContext.accessToken}`,
  };
}

/**
 * 获取已认证移动端上下文。
 *
 * <p>移动端业务 API 必须带租户和访问令牌，避免未登录状态下发起无租户请求。</p>
 *
 * @returns 已认证移动端用户上下文
 */
export function requireAuthenticatedMobileContext(): MobileUserContext & { userId: number; accessToken: string } {
  const userContext = getMobileUserContext();
  if (!userContext.loggedIn) {
    throw new Error('请先登录移动端账号');
  }
  if (userContext.userId === null || !Number.isFinite(userContext.userId)) {
    throw new Error('移动端用户 ID 缺失');
  }
  if (!userContext.tenantId) {
    throw new Error('移动端租户编码缺失');
  }
  if (!userContext.accessToken) {
    throw new Error('移动端访问令牌缺失');
  }
  return {
    userId: userContext.userId,
    orgId: userContext.orgId,
    accountName: userContext.accountName,
    tenantId: userContext.tenantId,
    roleName: userContext.roleName,
    accessToken: userContext.accessToken,
    loggedIn: userContext.loggedIn,
  };
}
