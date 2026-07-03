/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 系统用户岗位。
 */
export interface SystemUserPost {
  /** 岗位主键。 */
  postId: number;
  /** 岗位编码。 */
  postCode: string;
  /** 岗位名称。 */
  postName: string;
  /** 是否主岗位。 */
  primaryFlag: boolean;
}

/**
 * 用户岗位绑定参数。
 */
export interface SystemUserPostBindPayload {
  /** 岗位绑定项列表。 */
  posts: Array<{
    /** 岗位主键。 */
    postId: number;
    /** 是否主岗位。 */
    primaryFlag: boolean;
  }>;
}

/**
 * 查询系统用户岗位列表。
 *
 * @param tenantId 租户业务编码
 * @param userId 用户主键
 * @returns 系统用户岗位列表
 */
export function listSystemUserPosts(tenantId: string, userId: number): Promise<SystemUserPost[]> {
  return request<SystemUserPost[]>(`/system/users/${userId}/posts`, {
    query: {
      tenantId,
    },
  });
}

/**
 * 绑定系统用户岗位列表。
 *
 * @param tenantId 租户业务编码
 * @param userId 用户主键
 * @param payload 用户岗位绑定参数
 * @returns 空响应
 */
export function bindSystemUserPosts(
  tenantId: string,
  userId: number,
  payload: SystemUserPostBindPayload,
): Promise<void> {
  return request<void, SystemUserPostBindPayload>(`/system/users/${userId}/posts`, {
    method: 'PUT',
    query: {
      tenantId,
    },
    body: payload,
  });
}
