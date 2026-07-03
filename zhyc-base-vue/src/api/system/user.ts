/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统用户。
 */
export interface SystemUser {
  /** 用户主键。 */
  id: number;
  /** 租户业务编码。 */
  tenantId: string;
  /** 登录账号。 */
  username: string;
  /** 用户显示名称。 */
  nickname: string;
  /** 用户状态。 */
  status: string;
}

/**
 * 系统用户保存参数。
 */
export interface SystemUserSavePayload {
  /** 租户业务编码。 */
  tenantId: string;
  /** 登录账号。 */
  username: string;
  /** 用户显示名称。 */
  nickname: string;
  /** 初始密码或重置密码，编辑时可为空。 */
  password?: string;
  /** 用户状态。 */
  status: string;
}

/**
 * 查询系统用户列表。
 *
 * @param tenantId 租户业务编码
 * @returns 系统用户列表
 */
export function listSystemUsers(tenantId: string): Promise<SystemUser[]> {
  return request<SystemUser[]>('/system/users', {
    query: {
      tenantId,
    },
  });
}

/**
 * 新增系统用户。
 *
 * @param payload 用户保存参数
 * @returns 空响应
 */
export function createSystemUser(payload: SystemUserSavePayload): Promise<void> {
  return request<void, SystemUserSavePayload>('/system/users', {
    method: 'POST',
    body: payload,
  });
}

/**
 * 编辑系统用户。
 *
 * @param userId 用户主键
 * @param payload 用户保存参数
 * @returns 空响应
 */
export function updateSystemUser(userId: number, payload: SystemUserSavePayload): Promise<void> {
  return request<void, SystemUserSavePayload>(`/system/users/${userId}`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * 修改系统用户状态。
 *
 * @param userId 用户主键
 * @param tenantId 租户业务编码
 * @param status 用户状态
 * @returns 空响应
 */
export function updateSystemUserStatus(userId: number, tenantId: string, status: string): Promise<void> {
  return request<void, { tenantId: string; status: string }>(`/system/users/${userId}/status`, {
    method: 'PUT',
    body: {
      tenantId,
      status,
    },
  });
}

/**
 * 重置系统用户密码。
 *
 * @param userId 用户主键
 * @param tenantId 租户业务编码
 * @param password 新密码
 * @returns 空响应
 */
export function resetSystemUserPassword(userId: number, tenantId: string, password: string): Promise<void> {
  return request<void, { password: string }>(`/system/users/${userId}/password`, {
    method: 'PUT',
    query: {
      tenantId,
    },
    body: {
      password,
    },
  });
}

/**
 * 删除系统用户。
 *
 * @param userId 用户主键
 * @param tenantId 租户业务编码
 * @returns 空响应
 */
export function deleteSystemUser(userId: number, tenantId: string): Promise<void> {
  return request<void>(`/system/users/${userId}`, {
    method: 'DELETE',
    query: {
      tenantId,
    },
  });
}
