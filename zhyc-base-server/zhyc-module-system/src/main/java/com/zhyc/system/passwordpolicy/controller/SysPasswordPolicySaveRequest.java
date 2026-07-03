/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.passwordpolicy.controller;

/**
 * 系统密码策略保存请求。
 */
public class SysPasswordPolicySaveRequest {

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
}
