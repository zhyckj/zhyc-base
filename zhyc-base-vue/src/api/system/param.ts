/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统参数。
 */
export interface SystemParam {
  /** 系统参数主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 参数键。 */
  paramKey: string;
  /** 参数值。 */
  paramValue?: string;
  /** 参数值类型。 */
  valueType: string;
  /** 是否系统内置参数。 */
  systemFlag: boolean;
  /** 是否允许后台编辑。 */
  editable: boolean;
}

/**
 * 系统参数保存参数。
 */
export interface SystemParamSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 参数键。 */
  paramKey: string;
  /** 参数值。 */
  paramValue?: string;
  /** 参数值类型。 */
  valueType: string;
  /** 是否系统内置参数。 */
  systemFlag: boolean;
  /** 是否允许后台编辑。 */
  editable: boolean;
}

/**
 * 查询系统参数列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统参数列表
 */
export function listSystemParams(tenantId: string): Promise<SystemParam[]> {
  return request<SystemParam[]>('/system/params', {
    query: {
      tenantId,
    },
  });
}

/**
 * 保存系统参数。
 *
 * @param payload 系统参数保存参数
 * @returns 空响应
 */
export function saveSystemParam(payload: SystemParamSavePayload): Promise<void> {
  return request<void, SystemParamSavePayload>('/system/params', {
    method: 'PUT',
    body: payload,
  });
}
