/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统用户角色。
 */
export interface SystemUserRole {
  /** 角色主键。 */
  roleId: number;
  /** 角色编码。 */
  roleCode: string;
  /** 角色名称。 */
  roleName: string;
  /** 数据权限范围。 */
  dataScope: string;
  /** 角色状态。 */
  status: string;
}

/**
 * 用户角色绑定参数。
 */
export interface SystemUserRoleBindPayload {
  /** 角色主键列表。 */
  roleIds: number[];
}

/**
 * 查询系统用户角色列表。
 *
 * @param tenantId 租户业务编码
 * @param userId 用户主键
 * @returns 系统用户角色列表
 */
export function listSystemUserRoles(tenantId: string, userId: number): Promise<SystemUserRole[]> {
  return request<SystemUserRole[]>(`/system/users/${userId}/roles`, {
    query: {
      tenantId,
    },
  });
}

/**
 * 绑定系统用户角色列表。
 *
 * @param tenantId 租户业务编码
 * @param userId 用户主键
 * @param payload 用户角色绑定参数
 * @returns 空响应
 */
export function bindSystemUserRoles(
  tenantId: string,
  userId: number,
  payload: SystemUserRoleBindPayload,
): Promise<void> {
  return request<void, SystemUserRoleBindPayload>(`/system/users/${userId}/roles`, {
    method: 'PUT',
    query: {
      tenantId,
    },
    body: payload,
  });
}
