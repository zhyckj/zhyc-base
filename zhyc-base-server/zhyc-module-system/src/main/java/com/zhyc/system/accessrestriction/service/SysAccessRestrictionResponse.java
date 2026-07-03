/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.accessrestriction.service;

import java.time.LocalDateTime;

/**
 * 系统访问限制响应对象。
 */
public class SysAccessRestrictionResponse {

    /** 数据库主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 限制类型。 */
    private final String restrictionType;
    /** 规则值。 */
    private final String ruleValue;
    /** 生效动作。 */
    private final String effect;
    /** 生效开始时间。 */
    private final LocalDateTime startAt;
    /** 生效结束时间。 */
    private final LocalDateTime endAt;

    /**
     * 创建系统访问限制响应对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param restrictionType 限制类型
     * @param ruleValue 规则值
     * @param effect 生效动作
     * @param startAt 生效开始时间
     * @param endAt 生效结束时间
     */
    public SysAccessRestrictionResponse(Long id, String tenantId, String restrictionType, String ruleValue,
                                        String effect, LocalDateTime startAt, LocalDateTime endAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.restrictionType = restrictionType;
        this.ruleValue = ruleValue;
        this.effect = effect;
        this.startAt = startAt;
        this.endAt = endAt;
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
     * 返回租户业务编码。
     *
     * @return 租户业务编码
     */
    public String getTenantId() {
        return tenantId;
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
     * 返回规则值。
     *
     * @return 规则值
     */
    public String getRuleValue() {
        return ruleValue;
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
     * 返回生效开始时间。
     *
     * @return 生效开始时间
     */
    public LocalDateTime getStartAt() {
        return startAt;
    }

    /**
     * 返回生效结束时间。
     *
     * @return 生效结束时间
     */
    public LocalDateTime getEndAt() {
        return endAt;
    }
}
