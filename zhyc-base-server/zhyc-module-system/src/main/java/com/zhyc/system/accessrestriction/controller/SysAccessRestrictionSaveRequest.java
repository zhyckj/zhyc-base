/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.controller;

import java.time.LocalDateTime;

/**
 * 系统访问限制保存请求。
 */
public class SysAccessRestrictionSaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 限制类型。 */
    private String restrictionType;
    /** 规则值。 */
    private String ruleValue;
    /** 生效动作。 */
    private String effect;
    /** 生效开始时间。 */
    private LocalDateTime startAt;
    /** 生效结束时间。 */
    private LocalDateTime endAt;

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
     * 返回规则值。
     *
     * @return 规则值
     */
    public String getRuleValue() {
        return ruleValue;
    }

    /**
     * 设置规则值。
     *
     * @param ruleValue 规则值
     */
    public void setRuleValue(String ruleValue) {
        this.ruleValue = ruleValue;
    }

    /**
     * 返回生效动作。
     *
     * @return 生效动作
     */
    public String getEffect() {
        return effect;
    }

    /**
     * 设置生效动作。
     *
     * @param effect 生效动作
     */
    public void setEffect(String effect) {
        this.effect = effect;
    }

    /**
     * 返回生效开始时间。
     *
     * @return 生效开始时间
     */
    public LocalDateTime getStartAt() {
        return startAt;
    }

    /**
     * 设置生效开始时间。
     *
     * @param startAt 生效开始时间
     */
    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * 返回生效结束时间。
     *
     * @return 生效结束时间
     */
    public LocalDateTime getEndAt() {
        return endAt;
    }

    /**
     * 设置生效结束时间。
     *
     * @param endAt 生效结束时间
     */
    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
