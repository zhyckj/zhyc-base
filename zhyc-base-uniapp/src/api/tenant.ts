/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { mobileRequest } from './request';

/**
 * 移动端授权租户记录。
 */
export interface MobileAuthorizedTenant {
  /** 租户业务编码。 */
  tenantId: string;
  /** 租户名称。 */
  name: string;
  /** 租户状态。 */
  status: string;
  /** 租户隔离模式。 */
  isolationMode: string;
  /** 当前租户套餐 ID。 */
  packageId?: number;
}

/**
 * 查询当前账号可访问的租户列表。
 *
 * @param username 登录账号
 * @returns 授权租户列表
 */
export function listMobileAuthorizedTenants(username: string): Promise<MobileAuthorizedTenant[]> {
  return mobileRequest<MobileAuthorizedTenant[]>('/system/tenants/authorized', {
    query: {
      username,
    },
  });
}
