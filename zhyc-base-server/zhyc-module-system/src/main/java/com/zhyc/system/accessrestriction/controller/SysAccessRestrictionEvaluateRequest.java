/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.controller;

/**
 * 系统访问限制判定请求。
 */
public class SysAccessRestrictionEvaluateRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 限制类型，例如 ip、account、device。 */
    private String restrictionType;
    /** 待判定访问标识，例如 IP、账号或设备标识。 */
    private String accessValue;

    /**
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * 返回限制类型。
     *
     * @return 限制类型
     */
    public String getRestrictionType() {
        return restrictionType;
    }

    /**
     * 设置限制类型。
     *
     * @param restrictionType 限制类型
     */
    public void setRestrictionType(String restrictionType) {
        this.restrictionType = restrictionType;
    }

    /**
     * 返回待判定访问标识。
     *
     * @return 待判定访问标识
     */
    public String getAccessValue() {
        return accessValue;
    }

    /**
     * 设置待判定访问标识。
     *
     * @param accessValue 待判定访问标识
     */
    public void setAccessValue(String accessValue) {
        this.accessValue = accessValue;
    }
}
