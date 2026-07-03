/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.service;

/**
 * 系统编码规则响应对象。
 */
public class SysCodeRuleResponse {

    /** 编码规则编码。 */
    private final String ruleCode;
    /** 编码规则名称。 */
    private final String ruleName;
    /** 编码前缀。 */
    private final String prefix;
    /** 日期格式。 */
    private final String datePattern;
    /** 序列号长度。 */
    private final Integer sequenceLength;
    /** 当前序列值。 */
    private final Integer currentValue;
    /** 是否启用。 */
    private final boolean enabled;

    /**
     * 创建系统编码规则响应对象。
     *
     * @param ruleCode 编码规则编码
     * @param ruleName 编码规则名称
     * @param prefix 编码前缀
     * @param datePattern 日期格式
     * @param sequenceLength 序列号长度
     * @param currentValue 当前序列值
     * @param enabled 是否启用
     */
    public SysCodeRuleResponse(String ruleCode, String ruleName, String prefix, String datePattern,
                               Integer sequenceLength, Integer currentValue, boolean enabled) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.prefix = prefix;
        this.datePattern = datePattern;
        this.sequenceLength = sequenceLength;
        this.currentValue = currentValue;
        this.enabled = enabled;
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
     * 返回编码规则名称。
     *
     * @return 编码规则名称
     */
    public String getRuleName() {
        return ruleName;
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
     * 返回日期格式。
     *
     * @return 日期格式
     */
    public String getDatePattern() {
        return datePattern;
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
     * 返回当前序列值。
     *
     * @return 当前序列值
     */
    public Integer getCurrentValue() {
        return currentValue;
    }

    /**
     * 返回是否启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
}
