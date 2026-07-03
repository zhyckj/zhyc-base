/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 租户参数。
 */
export interface SystemTenantParam {
  /** 数据库主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 参数键。 */
  paramKey: string;
  /** 参数值。 */
  paramValue?: string;
  /** 参数值类型。 */
  valueType: string;
  /** 是否显示给租户管理员。 */
  visible: boolean;
}

/**
 * 租户参数保存参数。
 */
export interface SystemTenantParamSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 参数键。 */
  paramKey: string;
  /** 参数值。 */
  paramValue?: string;
  /** 参数值类型。 */
  valueType: string;
  /** 是否显示给租户管理员。 */
  visible: boolean;
}

/**
 * 查询租户参数列表。
 *
 * @param tenantId 租户业务编码
 * @returns 租户参数列表
 */
export function listSystemTenantParams(tenantId: string): Promise<SystemTenantParam[]> {
  return request<SystemTenantParam[]>('/system/tenant-params', {
    query: {
      tenantId,
    },
  });
}

/**
 * 保存租户参数。
 *
 * @param payload 租户参数保存参数
 * @returns 空响应
 */
export function saveSystemTenantParam(payload: SystemTenantParamSavePayload): Promise<void> {
  return request<void, SystemTenantParamSavePayload>('/system/tenant-params', {
    method: 'PUT',
    body: payload,
  });
}
