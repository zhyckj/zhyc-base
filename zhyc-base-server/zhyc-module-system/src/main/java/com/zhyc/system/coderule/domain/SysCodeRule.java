/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.coderule.domain;

import java.time.LocalDateTime;

/**
 * 系统编码规则模型。
 *
 * <p>用于生成业务单号、流程编号和低代码样板业务编码。</p>
 */
public class SysCodeRule {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 编码规则编码。 */
    private String ruleCode;
    /** 编码规则名称。 */
    private String ruleName;
    /** 编码前缀。 */
    private String prefix;
    /** 日期格式，例如 yyyyMMdd。 */
    private String datePattern;
    /** 序列号长度。 */
    private Integer sequenceLength;
    /** 当前序列值。 */
    private Integer currentValue;
    /** 是否启用。 */
    private boolean enabled;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空系统编码规则对象。
     */
    public SysCodeRule() {
    }

    /**
     * 创建完整系统编码规则对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param ruleCode 编码规则编码
     * @param ruleName 编码规则名称
     * @param prefix 编码前缀
     * @param datePattern 日期格式
     * @param sequenceLength 序列号长度
     * @param currentValue 当前序列值
     * @param enabled 是否启用
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysCodeRule(Long id, String tenantId, String ruleCode, String ruleName, String prefix, String datePattern,
                       Integer sequenceLength, Integer currentValue, boolean enabled, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.prefix = prefix;
        this.datePattern = datePattern;
        this.sequenceLength = sequenceLength;
        this.currentValue = currentValue;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
    }

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

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 返回更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
