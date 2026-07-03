/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 调用审计响应。
 */
export interface OpenApiCallAuditResponse {
  /** API 访问密钥。 */
  accessKey: string;
  /** API 业务编码。 */
  apiCode: string;
  /** HTTP 方法。 */
  httpMethod: string;
  /** 请求路径。 */
  requestPath: string;
  /** HTTP 响应状态码。 */
  responseStatus: number;
  /** 调用耗时毫秒。 */
  durationMs: number;
  /** 是否调用成功，1 是 0 否。 */
  success: number;
  /** 错误编码。 */
  errorCode?: string;
  /** 客户端 IP。 */
  clientIp?: string;
  /** 请求追踪 ID。 */
  requestId?: string;
  /** 调用时间。 */
  calledAt?: string;
}

/**
 * 查询指定应用的开放 API 调用审计列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns 开放 API 调用审计列表
 */
export function listOpenApiCallAudits(tenantId: string, appCode: string): Promise<OpenApiCallAuditResponse[]> {
  return request<OpenApiCallAuditResponse[]>('/openapi/call-audits', {
    query: {
      tenantId,
      appCode,
    },
  });
}

/**
 * 查询指定应用的开放 API 错误日志列表。
 *
 * @param tenantId 租户业务编码
 * @param appCode 开发者应用编码
 * @returns 开放 API 错误日志列表
 */
export function listOpenApiErrorLogs(tenantId: string, appCode: string): Promise<OpenApiCallAuditResponse[]> {
  return request<OpenApiCallAuditResponse[]>('/openapi/call-audits/errors', {
    query: {
      tenantId,
      appCode,
    },
  });
}
