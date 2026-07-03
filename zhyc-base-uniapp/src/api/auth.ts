/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { getMobileAuthApiBaseUrl } from '@/config/runtime';

import { mobileRequest } from './request';

/**
 * 移动端登录请求。
 */
export interface MobileLoginRequest {
  /** 认证中心登录账号。 */
  username: string;
  /** 认证中心登录密码；仅随登录请求提交，不写入本地缓存。 */
  password: string;
}

/**
 * 移动端登录响应。
 */
export interface MobileLoginResponse {
  /** 访问令牌；用于移动端 Bearer Token 请求头。 */
  accessToken: string;
  /** 令牌类型，通常为 Bearer。 */
  tokenType: string;
  /** 访问令牌有效期，单位秒。 */
  expiresIn: number;
  /** 当前租户编码。 */
  tenantId: string;
  /** 当前用户主键。 */
  userId: number;
  /** 当前账号名称。 */
  accountName: string;
}

/**
 * 移动端账号密码登录。
 *
 * @param request 登录请求
 * @returns 登录令牌响应
 */
export function loginMobileAccount(request: MobileLoginRequest): Promise<MobileLoginResponse> {
  return mobileRequest<MobileLoginResponse, MobileLoginRequest>('/mobile/auth/login', {
    method: 'POST',
    baseUrl: getMobileAuthApiBaseUrl(),
    auth: false,
    data: request,
  });
}
