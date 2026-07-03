/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import java.util.List;

/**
 * 用户角色绑定命令。
 */
public class SysUserRoleBindCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 用户主键。 */
    private final Long userId;
    /** 角色主键列表。 */
    private final List<Long> roleIds;

    /**
     * 创建用户角色绑定命令。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param roleIds 角色主键列表
     */
    public SysUserRoleBindCommand(String tenantId, Long userId, List<Long> roleIds) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.roleIds = roleIds;
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
     * 返回用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回角色主键列表。
     *
     * @return 角色主键列表
     */
    public List<Long> getRoleIds() {
        return roleIds;
    }
}
