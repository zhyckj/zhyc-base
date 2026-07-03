/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统菜单树节点。
 */
export interface SystemMenuTreeNode {
  /** 数据库主键。 */
  id: number;
  /** 父级菜单主键。 */
  parentId?: number;
  /** 菜单编码。 */
  menuCode: string;
  /** 菜单名称。 */
  name: string;
  /** 菜单类型。 */
  type: string;
  /** 前端路由路径。 */
  path?: string;
  /** 前端组件路径。 */
  component?: string;
  /** 权限标识。 */
  permission?: string;
  /** 排序号。 */
  sortOrder?: number;
  /** 菜单状态；用于状态标签展示和启停操作判断，后端菜单树接口必须返回。 */
  status: string;
  /** 子菜单节点。 */
  children?: SystemMenuTreeNode[];
}

export interface SystemMenuSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 父级菜单主键。 */
  parentId?: number;
  /** 菜单编码。 */
  menuCode: string;
  /** 菜单名称。 */
  name: string;
  /** 菜单类型。 */
  type: string;
  /** 前端路由路径。 */
  path?: string;
  /** 前端组件路径。 */
  component?: string;
  /** 权限标识。 */
  permission?: string;
  /** 排序号。 */
  sortOrder?: number;
  /** 菜单状态。 */
  status: string;
}

/**
 * 查询系统菜单树。
 *
 * @param tenantId 租户业务编码
 * @returns 系统菜单树
 */
export function listSystemMenuTree(tenantId: string, includeDisabled = false): Promise<SystemMenuTreeNode[]> {
  return request<SystemMenuTreeNode[]>('/system/menus/tree', {
    query: {
      tenantId,
      includeDisabled,
    },
  });
}

export function createSystemMenu(payload: SystemMenuSavePayload): Promise<void> {
  return request<void, SystemMenuSavePayload>('/system/menus', {
    method: 'POST',
    body: payload,
  });
}

export function updateSystemMenu(menuId: number, payload: SystemMenuSavePayload): Promise<void> {
  return request<void, SystemMenuSavePayload>(`/system/menus/${menuId}`, {
    method: 'PUT',
    body: payload,
  });
}

export function updateSystemMenuStatus(menuId: number, tenantId: string, status: string): Promise<void> {
  return request<void, { tenantId: string; status: string }>(`/system/menus/${menuId}/status`, {
    method: 'PUT',
    body: {
      tenantId,
      status,
    },
  });
}

export function deleteSystemMenu(menuId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/menus/${menuId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}
