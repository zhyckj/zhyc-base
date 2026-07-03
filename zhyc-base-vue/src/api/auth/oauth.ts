/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 后台 OAuth2 授权码换令牌请求。
 */
export interface AdminOAuthTokenExchangeRequest {
  /** 认证中心返回的授权码；只提交给核心平台 BFF 换取令牌。 */
  code: string;
  /** 后台管理端 OAuth2 回调地址；必须与认证中心注册值一致。 */
  redirectUri: string;
  /** PKCE 原始校验码；用于认证中心校验本次授权码属于同一浏览器会话。 */
  codeVerifier: string;
}

/**
 * 后台 OAuth2 刷新令牌请求。
 */
export interface AdminOAuthRefreshTokenRequest {
  /** 刷新令牌；只提交给核心平台 BFF 换取新的访问令牌。 */
  refreshToken: string;
}

/**
 * 后台 OAuth2 令牌响应。
 */
export interface AdminOAuthTokenResponse {
  /** 访问令牌；用于后台管理端 Bearer Token 请求头。 */
  accessToken: string;
  /** 刷新令牌；首期仅由后续刷新链路使用。 */
  refreshToken?: string;
  /** 令牌类型，通常为 Bearer。 */
  tokenType?: string;
  /** 访问令牌有效期，单位秒。 */
  expiresIn?: number;
  /** 授权范围。 */
  scope?: string;
}

/**
 * 后端统一响应结构。
 */
interface AuthApiResult<T> {
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
 * 通过核心平台 BFF 使用授权码交换令牌。
 *
 * <p>该调用不携带登录态请求头，也不包含 OAuth2 客户端密钥；客户端密钥只存在核心平台服务端。</p>
 *
 * @param request 授权码换令牌请求
 * @returns 后台 OAuth2 令牌响应
 */
export async function exchangeAdminOAuthCode(request: AdminOAuthTokenExchangeRequest): Promise<AdminOAuthTokenResponse> {
  const response = await fetch('/api/auth/oauth2/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    throw new Error(await resolveAuthHttpErrorMessage(response, '统一认证令牌交换失败'));
  }
  const result = (await response.json()) as AuthApiResult<AdminOAuthTokenResponse>;
  if (!result.success) {
    throw new Error(result.message || result.code || '统一认证令牌交换失败');
  }
  if (!result.data?.accessToken) {
    throw new Error('统一认证令牌响应缺少访问令牌');
  }
  return result.data;
}

/**
 * 通过核心平台 BFF 使用刷新令牌续期访问令牌。
 *
 * <p>该调用不包含 OAuth2 客户端密钥；客户端密钥只存在核心平台服务端。</p>
 *
 * @param request 刷新令牌请求
 * @returns 后台 OAuth2 令牌响应
 */
export async function refreshAdminOAuthToken(request: AdminOAuthRefreshTokenRequest): Promise<AdminOAuthTokenResponse> {
  const response = await fetch('/api/auth/oauth2/refresh', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  if (!response.ok) {
    throw new Error(await resolveAuthHttpErrorMessage(response, '统一认证令牌刷新失败'));
  }
  const result = (await response.json()) as AuthApiResult<AdminOAuthTokenResponse>;
  if (!result.success) {
    throw new Error(result.message || result.code || '统一认证令牌刷新失败');
  }
  if (!result.data?.accessToken) {
    throw new Error('统一认证刷新响应缺少访问令牌');
  }
  return result.data;
}

/**
 * 解析统一认证 BFF HTTP 错误响应。
 *
 * @param response fetch 响应
 * @param fallback 默认错误前缀
 * @returns 用户可读错误消息
 */
async function resolveAuthHttpErrorMessage(response: Response, fallback: string): Promise<string> {
  try {
    const result = (await response.json()) as Partial<AuthApiResult<unknown>>;
    if (typeof result.message === 'string' && result.message.trim()) {
      return result.message.trim();
    }
    if (typeof result.code === 'string' && result.code.trim()) {
      return result.code.trim();
    }
  } catch {
    // 非 JSON 响应保留状态码，避免暴露服务端 HTML 错误页。
  }
  return `${fallback}: ${response.status}`;
}
