/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.service;

import java.util.List;

/**
 * 管理员管理范围绑定命令。
 */
public class AdminScopeBindCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 管理员用户主键。 */
    private final Long userId;
    /** 管理员范围绑定项列表。 */
    private final List<AdminScopeBindItem> scopes;

    /**
     * 创建管理员管理范围绑定命令。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @param scopes 管理员范围绑定项列表
     */
    public AdminScopeBindCommand(String tenantId, Long userId, List<AdminScopeBindItem> scopes) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.scopes = scopes;
    }

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 返回管理员用户主键。
     *
     * @return 管理员用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回管理员范围绑定项列表。
     *
     * @return 管理员范围绑定项列表
     */
    public List<AdminScopeBindItem> getScopes() {
        return scopes;
    }
}
