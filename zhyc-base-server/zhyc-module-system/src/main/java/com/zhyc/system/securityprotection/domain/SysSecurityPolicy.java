/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.domain;

import java.time.LocalDateTime;

/**
 * 系统安全防护策略。
 *
 * <p>按租户维护运行时防护阈值和处置动作，用于后台请求、登录、开放 API 和 AI 调用等安全入口。</p>
 */
public class SysSecurityPolicy {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 策略编码，同一租户内唯一。 */
    private String policyCode;
    /** 策略名称。 */
    private String policyName;
    /** 防护范围，例如 admin_api、login、openapi、ai。 */
    private String protectionScope;
    /** 目标匹配表达式，例如接口路径、模型编码或星号。 */
    private String targetPattern;
    /** 时间窗口内允许的最大次数。 */
    private Integer thresholdLimit;
    /** 统计时间窗口秒数。 */
    private Integer windowSeconds;
    /** 触发后的处置动作，例如 observe、captcha、block。 */
    private String action;
    /** 自动封禁秒数；处置动作不是 block 时可为空。 */
    private Integer blockSeconds;
    /** 策略状态，enabled 或 disabled。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
