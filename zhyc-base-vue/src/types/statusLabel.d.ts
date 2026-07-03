/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import type { formatStatusLabel } from '@/utils/statusLabel';

declare module 'vue' {
  interface ComponentCustomProperties {
    /** 状态编码中文格式化函数，用于模板统一展示后端状态编码。 */
    $statusLabel: typeof formatStatusLabel;
  }
}

export {};
