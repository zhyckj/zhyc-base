/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.service;

import java.time.LocalDateTime;

/**
 * 安全事件记录命令。
 */
public class SecurityEventRecordCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 事件类型。 */
    private final String eventType;
    /** 事件等级。 */
    private final String eventLevel;
    /** 来源 IP。 */
    private final String sourceIp;
    /** 用户主键。 */
    private final Long userId;
    /** 用户账号。 */
    private final String username;
    /** 请求路径。 */
    private final String requestPath;
    /** HTTP 方法。 */
    private final String httpMethod;
    /** 处置动作。 */
    private final String action;
    /** 处置结果。 */
    private final String result;
    /** 事件描述。 */
    private final String message;
    /** 发生时间。 */
    private final LocalDateTime occurredAt;

    public SecurityEventRecordCommand(String tenantId, String eventType, String eventLevel, String sourceIp,
                                      Long userId, String username, String requestPath, String httpMethod,
                                      String action, String result, String message, LocalDateTime occurredAt) {
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.eventLevel = eventLevel;
        this.sourceIp = sourceIp;
        this.userId = userId;
        this.username = username;
        this.requestPath = requestPath;
        this.httpMethod = httpMethod;
        this.action = action;
        this.result = result;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventLevel() {
        return eventLevel;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getAction() {
        return action;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
