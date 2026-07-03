/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 开发者应用列表项。
 */
export interface OpenApiAppResponse {
  /** 应用主键。 */
  id: number;
  /** 租户编码。 */
  tenantId: string;
  /** 应用编码。 */
  appCode: string;
  /** 应用名称。 */
  appName: string;
  /** 应用状态。 */
  status: string;
}

/**
 * 查询开发者应用。
 */
export function listOpenApiApps(tenantId: string): Promise<OpenApiAppResponse[]> {
  return request<OpenApiAppResponse[]>('/openapi/apps', {
    query: { tenantId },
  });
}
