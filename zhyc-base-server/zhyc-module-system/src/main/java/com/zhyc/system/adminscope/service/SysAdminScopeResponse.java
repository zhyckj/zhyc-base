/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.service;

/**
 * 系统管理员管理范围响应对象。
 */
public class SysAdminScopeResponse {

    /** 范围类型，例如 tenant、org、module。 */
    private final String scopeType;
    /** 范围引用编码。 */
    private final String scopeRefCode;
    /** 范围展示名称。 */
    private final String scopeName;

    /**
     * 创建系统管理员管理范围响应对象。
     *
     * @param scopeType 范围类型
     * @param scopeRefCode 范围引用编码
     * @param scopeName 范围展示名称
     */
    public SysAdminScopeResponse(String scopeType, String scopeRefCode, String scopeName) {
        this.scopeType = scopeType;
        this.scopeRefCode = scopeRefCode;
        this.scopeName = scopeName;
    }

    /**
     * 返回范围类型。
     *
     * @return 范围类型
     */
    public String getScopeType() {
        return scopeType;
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
     * 返回范围展示名称。
     *
     * @return 范围展示名称
     */
    public String getScopeName() {
        return scopeName;
    }
}
