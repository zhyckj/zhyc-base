/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.lowcode.metadata.mybatis;

/**
 * 低代码页面模型持久化对象。
 */
public record LowcodePageModelRecord(
    Long id,
    String tenantId,
    Long tableModelId,
    String pageType,
    String routePath,
    String componentPath,
    String layoutType) {
}
