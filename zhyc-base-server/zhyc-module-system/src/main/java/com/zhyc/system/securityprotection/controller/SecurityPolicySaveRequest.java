/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.controller;

/**
 * 安全防护策略保存请求。
 */
public class SecurityPolicySaveRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** 策略编码。 */
    private String policyCode;
    /** 策略名称。 */
    private String policyName;
    /** 防护范围。 */
    private String protectionScope;
    /** 目标匹配表达式。 */
    private String targetPattern;
    /** 阈值次数。 */
    private Integer thresholdLimit;
    /** 统计窗口秒数。 */
    private Integer windowSeconds;
    /** 触发动作。 */
    private String action;
    /** 自动封禁秒数。 */
    private Integer blockSeconds;
    /** 策略状态。 */
    private String status;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getProtectionScope() {
        return protectionScope;
    }

    public void setProtectionScope(String protectionScope) {
        this.protectionScope = protectionScope;
    }

    public String getTargetPattern() {
        return targetPattern;
    }

    public void setTargetPattern(String targetPattern) {
        this.targetPattern = targetPattern;
    }

    public Integer getThresholdLimit() {
        return thresholdLimit;
    }

    public void setThresholdLimit(Integer thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
    }

    public Integer getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Integer windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getBlockSeconds() {
        return blockSeconds;
    }

    public void setBlockSeconds(Integer blockSeconds) {
        this.blockSeconds = blockSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
