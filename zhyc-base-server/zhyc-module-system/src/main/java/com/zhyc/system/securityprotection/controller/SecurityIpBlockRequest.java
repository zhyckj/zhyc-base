/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.controller;

import java.time.LocalDateTime;

/**
 * 安全防护 IP 封禁请求。
 */
public class SecurityIpBlockRequest {

    /** 租户业务编码。 */
    private String tenantId;
    /** IP、IPv6 或 CIDR 规则。 */
    private String ipValue;
    /** 封禁类型。 */
    private String blockType;
    /** 封禁原因。 */
    private String reason;
    /** 封禁开始时间。 */
    private LocalDateTime startAt;
    /** 封禁结束时间。 */
    private LocalDateTime endAt;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIpValue() {
        return ipValue;
    }

    public void setIpValue(String ipValue) {
        this.ipValue = ipValue;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
