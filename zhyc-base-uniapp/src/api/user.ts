/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import { mobileRequest } from './request';

/**
 * 移动端修改密码命令。
 */
export interface MobilePasswordChangeCommand {
  /** 租户业务编码；由移动端登录上下文提供。 */
  tenantId: string;
  /** 登录账号；由移动端登录上下文提供。 */
  username: string;
  /** 当前密码明文；仅用于服务端校验旧密码。 */
  oldPassword: string;
  /** 新密码明文；仅用于服务端策略校验和生成哈希。 */
  newPassword: string;
}

/**
 * 修改当前移动端用户密码。
 *
 * @param command 修改密码命令
 * @returns 修改是否成功
 */
export function changeMobilePassword(command: MobilePasswordChangeCommand): Promise<boolean> {
  return mobileRequest<boolean, MobilePasswordChangeCommand>('/system/users/password', {
    method: 'POST',
    data: command,
  });
}
