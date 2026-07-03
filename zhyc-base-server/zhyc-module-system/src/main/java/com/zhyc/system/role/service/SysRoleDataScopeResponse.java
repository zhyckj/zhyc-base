/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

/**
 * 系统角色自定义数据权限响应对象。
 */
public class SysRoleDataScopeResponse {

    /** 授权组织主键。 */
    private final Long orgId;
    /** 授权组织名称。 */
    private final String orgName;
    /** 范围类型，首期固定为 org。 */
    private final String scopeType;

    /**
     * 创建系统角色自定义数据权限响应对象。
     *
     * @param orgId 授权组织主键
     * @param orgName 授权组织名称
     * @param scopeType 范围类型
     */
    public SysRoleDataScopeResponse(Long orgId, String orgName, String scopeType) {
        this.orgId = orgId;
        this.orgName = orgName;
        this.scopeType = scopeType;
    }

    /**
     * 返回授权组织主键。
     *
     * @return 授权组织主键
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * 返回授权组织名称。
     *
     * @return 授权组织名称
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * 返回范围类型。
     *
     * @return 范围类型
     */
    public String getScopeType() {
        return scopeType;
    }
}
