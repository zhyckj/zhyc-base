/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 授权响应。
 */
export interface OpenApiPermissionResponse {
  /** API 业务编码。 */
  apiCode: string;
  /** API 名称。 */
  apiName: string;
  /** HTTP 方法。 */
  httpMethod: string;
  /** 请求路径匹配规则。 */
  pathPattern: string;
  /** 授权状态。 */
  status: string;
}

/**
 * 开放 API 授权保存参数。
 */
export interface OpenApiPermissionSaveRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode: string;
  /** API 业务编码。 */
  apiCode: string;
  /** API 名称。 */
  apiName: string;
  /** HTTP 方法。 */
  httpMethod: string;
  /** 请求路径匹配规则。 */
  pathPattern: string;
  /** 授权状态。 */
  status: string;
}

/**
 * 查询指定应用的开放 API 授权列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns 开放 API 授权列表
 */
export function listOpenApiPermissions(tenantId: string, appCode: string): Promise<OpenApiPermissionResponse[]> {
  return request<OpenApiPermissionResponse[]>('/openapi/api-permissions', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 保存开放 API 授权。
 *
 * @param command 开放 API 授权保存参数
 * @returns 空响应
 */
export function saveOpenApiPermission(command: OpenApiPermissionSaveRequest): Promise<void> {
  return request<void, OpenApiPermissionSaveRequest>('/openapi/api-permissions', {
    method: 'PUT',
    body: command,
  });
}
