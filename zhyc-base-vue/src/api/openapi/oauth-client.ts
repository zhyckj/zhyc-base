/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放平台 OAuth2 客户端映射响应。
 */
export interface OpenApiOauthClientResponse {
  /** 认证中心 OAuth2 客户端 ID。 */
  clientId: string;
  /** 允许的 OAuth2 授权范围，多个 scope 使用空格分隔。 */
  allowedScopes: string;
  /** 客户端映射状态。 */
  status: string;
}

/**
 * 开放平台 OAuth2 客户端映射保存参数。
 */
export interface OpenApiOauthClientSaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** 认证中心 OAuth2 客户端 ID。 */
  clientId: string;
  /** 允许的 OAuth2 授权范围。 */
  allowedScopes: string;
  /** 客户端映射状态。 */
  status: string;
}

/**
 * 查询指定应用的 OAuth2 客户端映射列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns OAuth2 客户端映射列表
 */
export function listOpenApiOauthClients(tenantId: string, appCode: string): Promise<OpenApiOauthClientResponse[]> {
  return request<OpenApiOauthClientResponse[]>('/openapi/oauth-clients', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 保存 OAuth2 客户端映射。
 *
 * @param command OAuth2 客户端映射保存参数
 * @returns 空响应
 */
export function saveOpenApiOauthClient(command: OpenApiOauthClientSaveRequest): Promise<void> {
  return request<void, OpenApiOauthClientSaveRequest>('/openapi/oauth-clients', {
    method: 'PUT',
    body: command,
  });
}
