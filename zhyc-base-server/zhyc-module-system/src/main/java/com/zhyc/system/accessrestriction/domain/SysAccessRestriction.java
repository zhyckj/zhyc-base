/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.domain;

import java.time.LocalDateTime;

/**
 * 系统访问限制模型。
 *
 * <p>用于承载 IP、账号、设备等访问限制规则，后续登录拦截器和网关策略可复用。</p>
 */
public class SysAccessRestriction {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 限制类型，例如 ip、account、device。 */
    private String restrictionType;
    /** 规则值，例如 IP、账号或设备标识。 */
    private String ruleValue;
    /** 生效动作，例如 allow 或 deny。 */
    private String effect;
    /** 生效开始时间，为空表示立即生效。 */
    private LocalDateTime startAt;
    /** 生效结束时间，为空表示长期生效。 */
    private LocalDateTime endAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建空系统访问限制对象。
     */
    public SysAccessRestriction() {
    }

    /**
     * 创建完整系统访问限制对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @param ruleValue 规则值
     * @param effect 生效动作
     * @param startAt 生效开始时间
     * @param endAt 生效结束时间
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysAccessRestriction(Long id, String tenantId, String restrictionType, String ruleValue, String effect,
                                LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.restrictionType = restrictionType;
        this.ruleValue = ruleValue;
        this.effect = effect;
        this.startAt = startAt;
        this.endAt = endAt;
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
