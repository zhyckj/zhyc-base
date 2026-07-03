/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

import java.time.LocalDateTime;

/**
 * 安全 IP 封禁命令。
 */
public class SecurityIpBlockCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** IP、IPv6 或 CIDR 规则值。 */
    private final String ipValue;
    /** 封禁类型。 */
    private final String blockType;
    /** 封禁原因。 */
    private final String reason;
    /** 开始时间。 */
    private final LocalDateTime startAt;
    /** 结束时间。 */
    private final LocalDateTime endAt;

    public SecurityIpBlockCommand(String tenantId, String ipValue, String blockType, String reason,
                                  LocalDateTime startAt, LocalDateTime endAt) {
        this.tenantId = tenantId;
        this.ipValue = ipValue;
        this.blockType = blockType;
        this.reason = reason;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getIpValue() {
        return ipValue;
    }

    public String getBlockType() {
        return blockType;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }
}
