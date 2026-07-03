/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统租户套餐。
 */
export interface SystemTenantPackage {
  /** 数据库主键。 */
  id: number;
  /** 套餐编码。 */
  packageCode: string;
  /** 套餐名称。 */
  packageName: string;
  /** 套餐状态。 */
  status: string;
  /** 最大用户数。 */
  maxUserCount: number;
  /** 最大存储容量，单位 MB。 */
  maxStorageMb: number;
  /** 创建时间。 */
  createdAt?: string;
  /** 更新时间。 */
  updatedAt?: string;
}

/**
 * 系统租户套餐创建参数。
 */
export interface SystemTenantPackageCreatePayload {
  /** 套餐编码；平台全局唯一。 */
  packageCode: string;
  /** 套餐名称；用于后台列表和租户选择。 */
  packageName: string;
  /** 套餐状态；enabled 启用，disabled 停用。 */
  status: string;
  /** 最大用户数；0 表示不限制。 */
  maxUserCount: number;
  /** 最大存储容量，单位 MB；0 表示不限制。 */
  maxStorageMb: number;
}

/**
 * 查询系统租户套餐列表。
 *
 * @param status 套餐状态
 * @returns 系统租户套餐列表
 */
export function listSystemTenantPackages(status: string): Promise<SystemTenantPackage[]> {
  return request<SystemTenantPackage[]>('/system/tenant-packages', {
    query: {
      status,
    },
  });
}

/**
 * 创建系统租户套餐。
 *
 * @param payload 租户套餐创建参数
 * @returns 创建后的系统租户套餐
 */
export function createSystemTenantPackage(
  payload: SystemTenantPackageCreatePayload,
): Promise<SystemTenantPackage> {
  return request<SystemTenantPackage, SystemTenantPackageCreatePayload>('/system/tenant-packages', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 修改系统租户套餐状态。
 *
 * @param packageCode 套餐编码
 * @param status 目标状态
 * @returns 空响应
 */
export function changeSystemTenantPackageStatus(packageCode: string, status: string): Promise<void> {
  return request<void, { status: string }>(`/system/tenant-packages/${packageCode}/status`, {
    method: 'PUT',
    body: {
      status,
    },
  });
}
