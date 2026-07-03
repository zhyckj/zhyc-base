/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.controller;

/**
 * 管理员管理范围绑定项请求。
 */
public class AdminScopeBindItemRequest {

    /** 范围类型，例如 tenant、org、module。 */
    private String scopeType;
    /** 范围引用编码。 */
    private String scopeRefCode;

    /**
     * 返回范围类型。
     *
     * @return 范围类型
     */
    public String getScopeType() {
        return scopeType;
    }

    /**
     * 设置范围类型。
     *
     * @param scopeType 范围类型
     */
    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    /**
     * 返回范围引用编码。
     *
     * @return 范围引用编码
     */
    public String getScopeRefCode() {
        return scopeRefCode;
    }

    /**
     * 设置范围引用编码。
     *
     * @param scopeRefCode 范围引用编码
     */
    public void setScopeRefCode(String scopeRefCode) {
        this.scopeRefCode = scopeRefCode;
    }
}
