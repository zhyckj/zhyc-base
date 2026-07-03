/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

/**
 * 移动端加载状态。
 */
export type MobileLoadStatus = 'idle' | 'loading' | 'success' | 'error';

/**
 * 移动端快捷入口。
 */
export interface MobileShortcut {
  /** 入口名称。 */
  title: string;
  /** 跳转地址。 */
  url: string;
  /** 业务说明。 */
  description: string;
}
