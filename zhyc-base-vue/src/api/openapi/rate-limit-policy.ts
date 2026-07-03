/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 限流策略响应。
 */
export interface OpenApiRateLimitPolicyResponse {
  /** 开放 API 业务编码。 */
  apiCode: string;
  /** 时间窗口内允许的最大调用次数。 */
  limitCount: number;
  /** 限流时间窗口，单位秒。 */
  windowSeconds: number;
  /** 限流策略状态。 */
  status: string;
}

/**
 * 开放 API 限流策略保存参数。
 */
export interface OpenApiRateLimitPolicySaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** 开放 API 业务编码。 */
  apiCode: string;
  /** 时间窗口内允许的最大调用次数。 */
  limitCount: number;
  /** 限流时间窗口，单位秒。 */
  windowSeconds: number;
  /** 限流策略状态。 */
  status: string;
}

/**
 * 查询指定应用的限流策略列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns 限流策略列表
 */
export function listOpenApiRateLimitPolicies(
  tenantId: string,
  appCode: string,
): Promise<OpenApiRateLimitPolicyResponse[]> {
  return request<OpenApiRateLimitPolicyResponse[]>('/openapi/rate-limit-policies', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 保存限流策略。
 *
 * @param command 限流策略保存参数
 * @returns 空响应
 */
export function saveOpenApiRateLimitPolicy(command: OpenApiRateLimitPolicySaveRequest): Promise<void> {
  return request<void, OpenApiRateLimitPolicySaveRequest>('/openapi/rate-limit-policies', {
    method: 'PUT',
    body: command,
  });
}
