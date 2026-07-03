/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 通用加载状态。
 */
export type LoadStatus = 'idle' | 'loading' | 'success' | 'error';

/**
 * 平台菜单入口。
 */
export interface PlatformShortcut {
  /** 快捷入口名称。 */
  title: string;
  /** 路由地址。 */
  path: string;
  /** 权限编码。 */
  permission: string;
}
