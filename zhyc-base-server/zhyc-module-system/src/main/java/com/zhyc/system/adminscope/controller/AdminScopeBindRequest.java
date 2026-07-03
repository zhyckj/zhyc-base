/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.controller;

import java.util.List;

/**
 * 管理员管理范围绑定请求。
 */
public class AdminScopeBindRequest {

    /** 管理员管理范围绑定项列表。 */
    private List<AdminScopeBindItemRequest> scopes;

    /**
     * 返回管理员管理范围绑定项列表。
     *
     * @return 管理员管理范围绑定项列表
     */
    public List<AdminScopeBindItemRequest> getScopes() {
        return scopes;
    }

    /**
     * 设置管理员管理范围绑定项列表。
     *
     * @param scopes 管理员管理范围绑定项列表
     */
    public void setScopes(List<AdminScopeBindItemRequest> scopes) {
        this.scopes = scopes;
    }
}
