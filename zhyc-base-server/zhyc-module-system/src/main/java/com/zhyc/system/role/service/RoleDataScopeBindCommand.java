/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import java.util.List;

/**
 * 角色自定义数据权限绑定命令。
 */
public class RoleDataScopeBindCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 角色主键。 */
    private final Long roleId;
    /** 授权组织主键列表。 */
    private final List<Long> orgIds;

    /**
     * 创建角色自定义数据权限绑定命令。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param orgIds 授权组织主键列表
     */
    public RoleDataScopeBindCommand(String tenantId, Long roleId, List<Long> orgIds) {
        this.tenantId = tenantId;
        this.roleId = roleId;
        this.orgIds = orgIds;
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
     * 返回角色主键。
     *
     * @return 角色主键
     */
    public Long getRoleId() {
        return roleId;
    }

    /**
     * 返回授权组织主键列表。
     *
     * @return 授权组织主键列表
     */
    public List<Long> getOrgIds() {
        return orgIds;
    }
}
