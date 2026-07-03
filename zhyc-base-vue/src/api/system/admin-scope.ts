/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 管理员管理范围。
 */
export interface SystemAdminScope {
  /** 范围类型，例如 tenant、org、module。 */
  scopeType: string;
  /** 范围引用编码。 */
  scopeRefCode: string;
  /** 范围展示名称。 */
  scopeName?: string;
}

/**
 * 管理员范围绑定参数。
 */
export interface SystemAdminScopeBindPayload {
  /** 管理员管理范围绑定项列表。 */
  scopes: Array<{
    /** 范围类型。 */
    scopeType: string;
    /** 范围引用编码。 */
    scopeRefCode: string;
  }>;
}

/**
 * 查询管理员管理范围。
 *
 * @param tenantId 租户业务编码
 * @param userId 管理员用户主键
 * @returns 管理员管理范围列表
 */
export function listSystemAdminScopes(tenantId: string, userId: number): Promise<SystemAdminScope[]> {
  return request<SystemAdminScope[]>(`/system/admins/${userId}/scopes`, {
    query: {
      tenantId,
    },
  });
}

/**
 * 绑定管理员管理范围。
 *
 * @param tenantId 租户业务编码
 * @param userId 管理员用户主键
 * @param payload 管理员范围绑定参数
 * @returns 空响应
 */
export function bindSystemAdminScopes(
  tenantId: string,
  userId: number,
  payload: SystemAdminScopeBindPayload,
): Promise<void> {
  return request<void, SystemAdminScopeBindPayload>(`/system/admins/${userId}/scopes`, {
    method: 'PUT',
    query: {
      tenantId,
    },
    body: payload,
  });
}
