/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 签名策略响应。
 */
export interface OpenApiSignaturePolicyResponse {
  /** 签名算法，首期支持 HMAC_SHA256。 */
  algorithm: string;
  /** 客户端时间戳允许偏差秒数。 */
  timestampToleranceSeconds: number;
  /** nonce 防重放有效期秒数。 */
  nonceTtlSeconds: number;
  /** 是否要求请求体参与摘要，1 是 0 否。 */
  requireBodyHash: number;
  /** 签名策略状态。 */
  status: string;
}

/**
 * 开放 API 签名策略保存参数。
 */
export interface OpenApiSignaturePolicySaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** 签名算法，首期支持 HMAC_SHA256。 */
  algorithm: string;
  /** 客户端时间戳允许偏差秒数。 */
  timestampToleranceSeconds: number;
  /** nonce 防重放有效期秒数。 */
  nonceTtlSeconds: number;
  /** 是否要求请求体参与摘要，1 是 0 否。 */
  requireBodyHash: number;
  /** 签名策略状态。 */
  status: string;
}

/**
 * 查询指定应用的签名策略列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns 签名策略列表
 */
export function listOpenApiSignaturePolicies(
  tenantId: string,
  appCode: string,
): Promise<OpenApiSignaturePolicyResponse[]> {
  return request<OpenApiSignaturePolicyResponse[]>('/openapi/signature-policies', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 保存签名策略。
 *
 * @param command 签名策略保存参数
 * @returns 空响应
 */
export function saveOpenApiSignaturePolicy(command: OpenApiSignaturePolicySaveRequest): Promise<void> {
  return request<void, OpenApiSignaturePolicySaveRequest>('/openapi/signature-policies', {
    method: 'PUT',
    body: command,
  });
}
