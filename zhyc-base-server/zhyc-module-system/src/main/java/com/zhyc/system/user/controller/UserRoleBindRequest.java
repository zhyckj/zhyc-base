/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.controller;

import java.util.List;

/**
 * 用户角色绑定请求。
 */
public class UserRoleBindRequest {

    /** 角色主键列表。 */
    private List<Long> roleIds;

    /**
     * 返回角色主键列表。
     *
     * @return 角色主键列表
     */
    public List<Long> getRoleIds() {
        return roleIds;
    }

    /**
     * 设置角色主键列表。
     *
     * @param roleIds 角色主键列表
     */
    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
