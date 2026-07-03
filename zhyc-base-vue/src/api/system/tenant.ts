/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统租户。
 */
export interface SystemTenant {
  /** 数据库主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 租户名称。 */
  name: string;
  /** 当前租户套餐主键。 */
  packageId?: number;
  /** 租户隔离模式。 */
  isolationMode: 'TENANT_COLUMN' | 'SCHEMA' | 'DATABASE';
  /** 租户状态。 */
  status: string;
  /** 租户联系人姓名。 */
  contactName?: string;
  /** 租户联系人电话。 */
  contactPhone?: string;
  /** 租户到期时间。 */
  expireAt?: string;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
}

/**
 * 系统租户创建参数。
 */
export interface SystemTenantCreatePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 租户名称。 */
  name: string;
  /** 当前租户套餐主键。 */
  packageId?: number;
  /** 租户隔离模式。 */
  isolationMode: 'TENANT_COLUMN' | 'SCHEMA' | 'DATABASE';
  /** 租户状态。 */
  status: string;
  /** 租户联系人姓名。 */
  contactName?: string;
  /** 租户联系人电话。 */
  contactPhone?: string;
  /** 租户到期时间，格式示例：2026-12-31T23:59:59。 */
  expireAt?: string;
}

/**
 * 查询系统租户列表。
 *
 * @param status 租户状态
 * @returns 系统租户列表
 */
export function listSystemTenants(status: string): Promise<SystemTenant[]> {
  return request<SystemTenant[]>('/system/tenants', {
    query: {
      status,
    },
  });
}

/**
 * 创建或更新系统租户。
 *
 * @param payload 系统租户创建参数
 * @returns 空响应
 */
export function createSystemTenant(payload: SystemTenantCreatePayload): Promise<void> {
  return request<void, SystemTenantCreatePayload>('/system/tenants', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 更新系统租户。
 *
 * @param tenantId 租户业务编码
 * @param payload 系统租户保存参数
 * @returns 空响应
 */
export function updateSystemTenant(tenantId: string, payload: SystemTenantCreatePayload): Promise<void> {
  return request<void, SystemTenantCreatePayload>(`/system/tenants/${tenantId}`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 修改系统租户状态。
 *
 * @param tenantId 租户业务编码
 * @param status 目标状态
 * @returns 空响应
 */
export function changeSystemTenantStatus(tenantId: string, status: string): Promise<void> {
  return request<void, { status: string }>(`/system/tenants/${tenantId}/status`, {
    method: 'PUT',
    body: {
      status,
    },
  });
}

/**
 * 删除系统租户主记录。
 *
 * @param tenantId 租户业务编码
 * @returns 空响应
 */
export function deleteSystemTenant(tenantId: string): Promise<void> {
  return request<void>(`/system/tenants/${tenantId}`, {
    method: 'DELETE',
  });
}
