/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统角色。
 */
export interface SystemRole {
  /** 角色主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 角色编码。 */
  roleCode: string;
  /** 角色名称。 */
  name: string;
  /** 数据权限范围。 */
  dataScope?: string;
  /** 角色状态。 */
  status: string;
}

/**
 * 角色菜单授权参数。
 */
export interface SystemRoleMenuBindPayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 菜单主键列表。 */
  menuIds: number[];
}

/**
 * 系统角色保存参数。
 */
export interface SystemRoleSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 角色编码。 */
  roleCode: string;
  /** 角色名称。 */
  name: string;
  /** 数据权限范围。 */
  dataScope: string;
  /** 角色状态。 */
  status: string;
}

/**
 * 查询系统角色列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统角色列表
 */
export function listSystemRoles(tenantId: string): Promise<SystemRole[]> {
  return request<SystemRole[]>('/system/roles', {
    query: {
      tenantId,
    },
  });
}

/**
 * 新增系统角色。
 *
 * @param payload 系统角色保存参数
 * @returns 空响应
 */
export function createSystemRole(payload: SystemRoleSavePayload): Promise<void> {
  return request<void, SystemRoleSavePayload>('/system/roles', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 更新系统角色。
 *
 * @param roleId 角色主键
 * @param payload 系统角色保存参数
 * @returns 空响应
 */
export function updateSystemRole(roleId: number, payload: SystemRoleSavePayload): Promise<void> {
  return request<void, SystemRoleSavePayload>(`/system/roles/${roleId}`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 更新系统角色状态。
 *
 * @param roleId 角色主键
 * @param tenantId 租户业务编码
 * @param status 角色状态
 * @returns 空响应
 */
export function updateSystemRoleStatus(roleId: number, tenantId: string, status: string): Promise<void> {
  return request<void, { tenantId: string; status: string }>(`/system/roles/${roleId}/status`, {
    method: 'PUT',
    body: {
      tenantId,
      status,
    },
  });
}

/**
 * 删除系统角色。
 *
 * @param roleId 角色主键
 * @param tenantId 租户业务编码
 * @returns 空响应
 */
export function deleteSystemRole(roleId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/roles/${roleId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}

/**
 * 查询系统角色已绑定的菜单权限。
 *
 * @param roleId 角色主键
 * @param tenantId 租户业务编码
 * @returns 已绑定菜单主键列表
 */
export function listSystemRoleMenuIds(roleId: number, tenantId: string): Promise<number[]> {
  return request<number[]>(`/system/roles/${roleId}/menus`, {
    query: {
      tenantId,
    },
  });
}

/**
 * 绑定系统角色菜单权限。
 *
 * @param roleId 角色主键
 * @param payload 角色菜单授权参数
 * @returns 空响应
 */
export function bindSystemRoleMenus(roleId: number, payload: SystemRoleMenuBindPayload): Promise<void> {
  return request<void, SystemRoleMenuBindPayload>(`/system/roles/${roleId}/menus`, {
    method: 'PUT',
    body: payload,
  });
}
