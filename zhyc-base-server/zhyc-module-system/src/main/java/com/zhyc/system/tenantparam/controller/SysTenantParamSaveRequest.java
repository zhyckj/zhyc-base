/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.controller;

/**
 * 租户参数保存请求。
 */
public class SysTenantParamSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 参数键。 */
    private String paramKey;
    /** 参数值。 */
    private String paramValue;
    /** 参数值类型。 */
    private String valueType;
    /** 是否显示给租户管理员。 */
    private boolean visible = true;

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
     * 返回参数键。
     *
     * @return 参数键
     */
    public String getParamKey() {
        return paramKey;
    }

    /**
     * 设置参数键。
     *
     * @param paramKey 参数键
     */
    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    /**
     * 返回参数值。
     *
     * @return 参数值
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * 设置参数值。
     *
     * @param paramValue 参数值
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * 返回参数值类型。
     *
     * @return 参数值类型
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * 设置参数值类型。
     *
     * @param valueType 参数值类型
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    /**
     * 返回是否显示给租户管理员。
     *
     * @return 是否显示给租户管理员
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 设置是否显示给租户管理员。
     *
     * @param visible 是否显示给租户管理员
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
