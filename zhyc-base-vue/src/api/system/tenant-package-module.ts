/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 租户套餐模块授权项。
 */
export interface TenantPackageModuleGrant {
  /** 数据库主键。 */
  id?: number;
  /** 租户套餐主键。 */
  packageId: number;
  /** 模块编码。 */
  moduleCode: string;
  /** 菜单编码。 */
  menuCode?: string;
  /** 权限标识。 */
  permission?: string;
  /** 创建时间。 */
  createdAt?: string;
}

/**
 * 租户套餐模块授权绑定参数。
 */
export interface TenantPackageModuleBindPayload {
  /** 授权项列表。 */
  grants: Array<{
    /** 模块编码。 */
    moduleCode: string;
    /** 菜单编码。 */
    menuCode?: string;
    /** 权限标识。 */
    permission?: string;
  }>;
}

/**
 * 查询租户套餐模块授权列表。
 *
 * @param packageId 租户套餐主键
 * @returns 租户套餐模块授权列表
 */
export function listTenantPackageModuleGrants(packageId: number): Promise<TenantPackageModuleGrant[]> {
  return request<TenantPackageModuleGrant[]>(`/system/tenant-package-modules/${packageId}`);
}

/**
 * 绑定租户套餐模块授权。
 *
 * @param packageId 租户套餐主键
 * @param payload 授权绑定参数
 * @returns 空响应
 */
export function bindTenantPackageModuleGrants(
  packageId: number,
  payload: TenantPackageModuleBindPayload,
): Promise<void> {
  return request<void, TenantPackageModuleBindPayload>(`/system/tenant-package-modules/${packageId}`, {
    method: 'PUT',
    body: payload,
  });
}
