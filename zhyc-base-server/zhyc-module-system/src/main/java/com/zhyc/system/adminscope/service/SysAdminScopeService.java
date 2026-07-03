/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.service;

import java.util.List;

/**
 * 系统管理员管理范围业务服务。
 */
public interface SysAdminScopeService {

    /**
     * 查询管理员管理范围列表。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @return 管理员管理范围列表
     */
    List<SysAdminScopeResponse> listAdminScopes(String tenantId, Long userId);

    /**
     * 绑定管理员管理范围。
     *
     * @param command 管理员管理范围绑定命令
     */
    void bindAdminScopes(AdminScopeBindCommand command);
}
