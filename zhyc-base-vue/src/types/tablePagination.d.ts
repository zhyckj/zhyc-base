/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import type { tablePagination } from '@/utils/tablePagination';

declare module 'vue' {
  interface ComponentCustomProperties {
    /** 后台表格默认分页配置，用于模板统一开启分页展示。 */
    $tablePagination: typeof tablePagination;
  }
}

export {};
