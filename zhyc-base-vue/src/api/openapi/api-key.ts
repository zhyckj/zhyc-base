/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放平台 API Key 响应。
 */
export interface OpenApiApiKeyResponse {
  /** 开发者应用编码。 */
  appCode: string;
  /** API 访问密钥。 */
  accessKey: string;
  /** API Secret 掩码。 */
  secretMask: string;
  /** API Key 状态。 */
  status: string;
  /** 凭证过期时间。 */
  expireAt?: string;
}

/**
 * 开放平台 API Key 保存参数。
 */
export interface OpenApiApiKeySaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** API 访问密钥。 */
  accessKey: string;
  /** API Secret 密文。 */
  secretCipher: string;
  /** API Key 状态。 */
  status: string;
  /** 凭证过期时间。 */
  expireAt?: string;
}

/**
 * 开放平台 API Key Secret 轮换参数。
 */
export interface OpenApiApiKeyRotateRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** 新 API Secret 密文。 */
  secretCipher: string;
  /** 新凭证过期时间。 */
  expireAt?: string;
}

/**
 * 查询指定应用的 API Key 列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns API Key 列表
 */
export function listOpenApiApiKeys(tenantId: string, appCode: string): Promise<OpenApiApiKeyResponse[]> {
  return request<OpenApiApiKeyResponse[]>('/openapi/api-keys', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 保存开放平台 API Key。
 *
 * @param command API Key 保存参数
 * @returns 空响应
 */
export function saveOpenApiApiKey(command: OpenApiApiKeySaveRequest): Promise<void> {
  return request<void, OpenApiApiKeySaveRequest>('/openapi/api-keys', {
    method: 'PUT',
    body: command,
  });
}

/**
 * 轮换开放平台 API Key Secret。
 *
 * @param accessKey API 访问密钥
 * @param command API Key Secret 轮换参数
 * @returns 空响应
 */
export function rotateOpenApiApiKeySecret(accessKey: string, command: OpenApiApiKeyRotateRequest): Promise<void> {
  return request<void, OpenApiApiKeyRotateRequest>(`/openapi/api-keys/${encodeURIComponent(accessKey)}/rotate`, {
    method: 'POST',
    body: command,
  });
}
