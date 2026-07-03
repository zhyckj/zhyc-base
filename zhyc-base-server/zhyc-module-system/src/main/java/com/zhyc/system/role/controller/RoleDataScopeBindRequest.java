/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.controller;

import java.util.List;

/**
 * 角色自定义数据权限绑定请求。
 */
public class RoleDataScopeBindRequest {

    /** 授权组织主键列表。 */
    private List<Long> orgIds;

    /**
     * 返回授权组织主键列表。
     *
     * @return 授权组织主键列表
     */
    public List<Long> getOrgIds() {
        return orgIds;
    }

    /**
     * 设置授权组织主键列表。
     *
     * @param orgIds 授权组织主键列表
     */
    public void setOrgIds(List<Long> orgIds) {
        this.orgIds = orgIds;
    }
}
