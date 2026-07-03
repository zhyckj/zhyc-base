/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { request } from '@/api/http';

/**
 * 查询当前登录用户权限编码。
 *
 * @returns 当前登录用户可用权限编码列表
 */
export function listCurrentUserPermissions(): Promise<string[]> {
  return request<string[]>('/system/permissions/current');
}
