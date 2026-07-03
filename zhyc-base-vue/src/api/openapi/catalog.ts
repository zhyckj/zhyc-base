/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 目录响应。
 */
export interface OpenApiCatalogResponse {
  /** API 业务编码。 */
  apiCode: string;
  /** API 名称。 */
  apiName: string;
  /** API 分组编码。 */
  groupCode: string;
  /** HTTP 方法。 */
  httpMethod: string;
  /** 请求路径匹配规则。 */
  pathPattern: string;
  /** API 目录状态。 */
  status: string;
}

/**
 * 开放 API 目录保存参数。
 */
export interface OpenApiCatalogSaveRequest {
  /** API 业务编码。 */
  apiCode: string;
  /** API 名称。 */
  apiName: string;
  /** API 分组编码。 */
  groupCode: string;
  /** HTTP 方法。 */
  httpMethod: string;
  /** 请求路径匹配规则。 */
  pathPattern: string;
  /** API 目录状态。 */
  status: string;
}

/**
 * 查询指定分组的开放 API 目录列表。
 *
 * @param groupCode API 分组编码
 * @returns 开放 API 目录列表
 */
export function listOpenApiCatalogs(groupCode: string): Promise<OpenApiCatalogResponse[]> {
  return request<OpenApiCatalogResponse[]>('/openapi/catalogs', {
    query: {
      groupCode,
    },
  });
}

/**
 * 保存开放 API 目录。
 *
 * @param command 开放 API 目录保存参数
 * @returns 空响应
 */
export function saveOpenApiCatalog(command: OpenApiCatalogSaveRequest): Promise<void> {
  return request<void, OpenApiCatalogSaveRequest>('/openapi/catalogs', {
    method: 'PUT',
    body: command,
  });
}
