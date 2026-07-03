/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.domain;

import java.time.LocalDateTime;

/**
 * 系统密码策略领域对象。
 */
public class SysPasswordPolicy {

    /** 主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 策略编码。 */
    private String policyCode;
    /** 策略名称。 */
    private String policyName;
    /** 密码最小长度。 */
    private Integer minLength;
    /** 是否要求包含大写字母。 */
    private boolean requireUppercase;
    /** 是否要求包含小写字母。 */
    private boolean requireLowercase;
    /** 是否要求包含数字。 */
    private boolean requireDigit;
    /** 是否要求包含特殊字符。 */
    private boolean requireSpecial;
    /** 密码有效天数。 */
    private Integer expireDays;
    /** 历史密码记忆次数。 */
    private Integer historyCount;
    /** 最大连续失败次数。 */
    private Integer maxRetryCount;
    /** 账号锁定分钟数。 */
    private Integer lockMinutes;
    /** 是否启用。 */
    private boolean enabled;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    /**
     * 创建系统密码策略领域对象。
     *
     * @param id 主键
     * @param tenantId 租户业务编码
     * @param policyCode 策略编码
     * @param policyName 策略名称
     * @param minLength 密码最小长度
     * @param requireUppercase 是否要求包含大写字母
     * @param requireLowercase 是否要求包含小写字母
     * @param requireDigit 是否要求包含数字
     * @param requireSpecial 是否要求包含特殊字符
     * @param expireDays 密码有效天数
     * @param historyCount 历史密码记忆次数
     * @param maxRetryCount 最大连续失败次数
     * @param lockMinutes 账号锁定分钟数
     * @param enabled 是否启用
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     */
    public SysPasswordPolicy(Long id, String tenantId, String policyCode, String policyName, Integer minLength,
                             boolean requireUppercase, boolean requireLowercase, boolean requireDigit,
                             boolean requireSpecial, Integer expireDays, Integer historyCount,
                             Integer maxRetryCount, Integer lockMinutes, boolean enabled,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.policyCode = policyCode;
        this.policyName = policyName;
        this.minLength = minLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigit = requireDigit;
        this.requireSpecial = requireSpecial;
        this.expireDays = expireDays;
        this.historyCount = historyCount;
        this.maxRetryCount = maxRetryCount;
        this.lockMinutes = lockMinutes;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public boolean isRequireUppercase() {
        return requireUppercase;
    }

    public boolean isRequireLowercase() {
        return requireLowercase;
    }

    public boolean isRequireDigit() {
        return requireDigit;
    }

    public boolean isRequireSpecial() {
        return requireSpecial;
    }

    public Integer getExpireDays() {
        return expireDays;
    }

    public Integer getHistoryCount() {
        return historyCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public Integer getLockMinutes() {
        return lockMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
