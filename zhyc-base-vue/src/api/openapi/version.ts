/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开放 API 版本发布响应。
 */
export interface OpenApiVersionResponse {
  /** API 版本号。 */
  version: string;
  /** 后端转发路由。 */
  backendRoute: string;
  /** 请求 JSON Schema。 */
  requestSchema: string;
  /** 响应 JSON Schema。 */
  responseSchema: string;
  /** API 版本状态。 */
  status: string;
}

/**
 * 开放 API 版本发布参数。
 */
export interface OpenApiVersionPublishRequest {
  /** API 业务编码。 */
  apiCode: string;
  /** API 版本号。 */
  version: string;
  /** 后端转发路由。 */
  backendRoute: string;
  /** 请求 JSON Schema。 */
  requestSchema: string;
  /** 响应 JSON Schema。 */
  responseSchema: string;
  /** API 版本状态。 */
  status: string;
}

/**
 * 查询指定 API 的版本列表。
 *
 * @param apiCode API 业务编码
 * @returns API 版本列表
 */
export function listOpenApiVersions(apiCode: string): Promise<OpenApiVersionResponse[]> {
  return request<OpenApiVersionResponse[]>('/openapi/versions', {
    query: {
      apiCode,
    },
  });
}

/**
 * 发布或更新开放 API 版本。
 *
 * @param command API 版本发布参数
 * @returns 空响应
 */
export function publishOpenApiVersion(command: OpenApiVersionPublishRequest): Promise<void> {
  return request<void, OpenApiVersionPublishRequest>('/openapi/versions', {
    method: 'PUT',
    body: command,
  });
}
