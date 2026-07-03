/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.common.tenant;

/**
 * 租户数据隔离模式。
 */
public enum TenantIsolationMode {
    /** 多租户共用表，通过租户字段区分数据。 */
    TENANT_COLUMN,
    /** 每个租户使用独立数据库 schema。 */
    SCHEMA,
    /** 每个租户使用独立数据库。 */
    DATABASE
}
