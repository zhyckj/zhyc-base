/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.controller;

/**
 * 系统编码规则保存请求。
 */
public class SysCodeRuleSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 编码规则编码。 */
    private String ruleCode;
    /** 编码规则名称。 */
    private String ruleName;
    /** 编码前缀。 */
    private String prefix;
    /** 日期格式。 */
    private String datePattern;
    /** 序列号长度。 */
    private Integer sequenceLength;
    /** 当前序列值。 */
    private Integer currentValue;
    /** 是否启用。 */
    private boolean enabled;

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
     * 返回编码规则编码。
     *
     * @return 编码规则编码
     */
    public String getRuleCode() {
        return ruleCode;
    }

    /**
     * 设置编码规则编码。
     *
     * @param ruleCode 编码规则编码
     */
    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    /**
     * 返回编码规则名称。
     *
     * @return 编码规则名称
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * 设置编码规则名称。
     *
     * @param ruleName 编码规则名称
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * 返回编码前缀。
     *
     * @return 编码前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 设置编码前缀。
     *
     * @param prefix 编码前缀
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 返回日期格式。
     *
     * @return 日期格式
     */
    public String getDatePattern() {
        return datePattern;
    }

    /**
     * 设置日期格式。
     *
     * @param datePattern 日期格式
     */
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * 返回序列号长度。
     *
     * @return 序列号长度
     */
    public Integer getSequenceLength() {
        return sequenceLength;
    }

    /**
     * 设置序列号长度。
     *
     * @param sequenceLength 序列号长度
     */
    public void setSequenceLength(Integer sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    /**
     * 返回当前序列值。
     *
     * @return 当前序列值
     */
    public Integer getCurrentValue() {
        return currentValue;
    }

    /**
     * 设置当前序列值。
     *
     * @param currentValue 当前序列值
     */
    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * 返回是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
