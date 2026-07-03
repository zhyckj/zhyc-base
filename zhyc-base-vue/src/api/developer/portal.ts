/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { listOpenApiApiKeys, type OpenApiApiKeyResponse } from '@/api/openapi/api-key';
import { listOpenApiApps, type OpenApiAppResponse } from '@/api/openapi/app';
import { listOpenApiCallAudits, type OpenApiCallAuditResponse } from '@/api/openapi/call-audit';
import { listOpenApiCatalogs, type OpenApiCatalogResponse } from '@/api/openapi/catalog';
import { listOpenApiOauthClients, type OpenApiOauthClientResponse } from '@/api/openapi/oauth-client';
import { request } from '@/api/http';

/**
 * 开发者门户概览查询参数。
 */
export interface DeveloperPortalOverviewQuery {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开发者应用编码。 */
  appCode?: string;
  /** API 分组编码。 */
  groupCode: string;
}

/**
 * 开发者门户概览响应。
 */
export interface DeveloperPortalOverview {
  /** 开发者应用列表。 */
  apps: OpenApiAppResponse[];
  /** API Key 凭证列表。 */
  apiKeys: OpenApiApiKeyResponse[];
  /** OAuth2 客户端映射列表。 */
  oauthClients: OpenApiOauthClientResponse[];
  /** API 目录列表。 */
  catalogs: OpenApiCatalogResponse[];
  /** 开放 API 调用记录。 */
  callAudits: OpenApiCallAuditResponse[];
}

/**
 * 开放 API 调试请求。
 */
export interface OpenApiDebugInvokeRequest {
  /** 租户业务编码。 */
  tenantId: string;
  /** 开放 API 业务编码。 */
  apiCode: string;
  /** HTTP 请求方法。 */
  method: string;
  /** 开放 API 网关路径。 */
  path: string;
  /** 调试认证方式。 */
  authMode: 'API_KEY' | 'OAUTH2';
  /** API Key Access Key。 */
  accessKey?: string;
  /** API Key 签名时间戳。 */
  timestamp?: string;
  /** API Key 签名随机串。 */
  nonce?: string;
  /** API Key 签名值。 */
  signature?: string;
  /** OAuth2/OIDC 访问令牌。 */
  bearerToken?: string;
  /** 请求追踪编号。 */
  requestId: string;
  /** 请求体文本。 */
  body: string;
}

/**
 * 开放 API 调试响应。
 */
export interface OpenApiDebugResponse {
  /** 请求追踪编号。 */
  requestId: string;
  /** 开放 API 业务编码。 */
  apiCode: string;
  /** 网关或后端响应状态码。 */
  httpStatus: number;
  /** 调试调用是否成功。 */
  success: boolean;
  /** 对外稳定错误码。 */
  errorCode?: string;
  /** 网关调用耗时毫秒数。 */
  costMillis: number;
  /** 响应体文本。 */
  responseBody: string;
}

/**
 * 加载开发者门户概览数据。
 *
 * @param query 门户概览查询参数
 * @returns 门户概览数据
 */
export async function loadDeveloperPortalOverview(
  query: DeveloperPortalOverviewQuery,
): Promise<DeveloperPortalOverview> {
  const apps = await listOpenApiApps(query.tenantId);
  const selectedAppCode = query.appCode || apps[0]?.appCode || '';
  const [apiKeys, oauthClients, catalogs, callAudits] = await Promise.all([
    selectedAppCode ? listOpenApiApiKeys(query.tenantId, selectedAppCode) : Promise.resolve([]),
    selectedAppCode ? listOpenApiOauthClients(query.tenantId, selectedAppCode) : Promise.resolve([]),
    listOpenApiCatalogs(query.groupCode),
    selectedAppCode ? listOpenApiCallAudits(query.tenantId, selectedAppCode) : Promise.resolve([]),
  ]);

  return {
    apps,
    apiKeys,
    oauthClients,
    catalogs,
    callAudits,
  };
}

/**
 * 调用后台开放 API 调试代理。
 *
 * @param body 调试请求体
 * @returns 调试代理响应
 */
export function invokeOpenApiDebug(body: OpenApiDebugInvokeRequest): Promise<OpenApiDebugResponse> {
  return request<OpenApiDebugResponse, OpenApiDebugInvokeRequest>('/openapi/debug/invoke', {
    method: 'POST',
    body,
  });
}
