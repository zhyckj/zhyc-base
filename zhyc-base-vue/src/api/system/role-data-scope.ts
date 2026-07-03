/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 角色数据权限范围。
 */
export interface SystemRoleDataScope {
  /** 授权组织主键。 */
  orgId: number;
  /** 授权组织名称。 */
  orgName: string;
  /** 范围类型。 */
  scopeType: string;
}

/**
 * 角色数据权限绑定参数。
 */
export interface SystemRoleDataScopeBindPayload {
  /** 授权组织主键列表。 */
  orgIds: number[];
}

/**
 * 查询角色数据权限范围。
 *
 * @param tenantId 租户业务编码
 * @param roleId 角色主键
 * @returns 角色数据权限范围列表
 */
export function listSystemRoleDataScopes(tenantId: string, roleId: number): Promise<SystemRoleDataScope[]> {
  return request<SystemRoleDataScope[]>(`/system/roles/${roleId}/data-scopes`, {
    query: {
      tenantId,
    },
  });
}

/**
 * 绑定角色数据权限范围。
 *
 * @param tenantId 租户业务编码
 * @param roleId 角色主键
 * @param payload 角色数据权限绑定参数
 * @returns 空响应
 */
export function bindSystemRoleDataScopes(
  tenantId: string,
  roleId: number,
  payload: SystemRoleDataScopeBindPayload,
): Promise<void> {
  return request<void, SystemRoleDataScopeBindPayload>(`/system/roles/${roleId}/data-scopes`, {
    method: 'PUT',
    query: {
      tenantId,
    },
    body: payload,
  });
}
