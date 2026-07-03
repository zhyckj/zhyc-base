/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission;

/**
 * 数据权限范围。
 *
 * <p>用于描述角色或授权策略可访问的数据集合。</p>
 */
public enum DataScope {
    /** 仅本人数据。 */
    SELF,
    /** 当前部门数据。 */
    CURRENT_DEPT,
    /** 当前部门及下级部门数据。 */
    CURRENT_DEPT_AND_CHILDREN,
    /** 自定义数据范围。 */
    CUSTOM,
    /** 全部数据。 */
    ALL
}
