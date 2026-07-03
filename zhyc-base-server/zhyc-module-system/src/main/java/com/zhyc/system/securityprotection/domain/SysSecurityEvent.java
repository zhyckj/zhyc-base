/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.securityprotection.domain;

import java.time.LocalDateTime;

/**
 * 系统安全事件。
 *
 * <p>用于记录后台、开放 API、AI、登录等入口的访问风险、封禁结果和处置轨迹。</p>
 */
public class SysSecurityEvent {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 事件类型，例如 request_rate、login_fail、ip_block。 */
    private String eventType;
    /** 事件等级，例如 low、medium、high、critical。 */
    private String eventLevel;
    /** 请求来源 IP。 */
    private String sourceIp;
    /** 关联用户主键。 */
    private Long userId;
    /** 关联用户账号。 */
    private String username;
    /** 请求路径。 */
    private String requestPath;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 处置动作，例如 observe、block、allow。 */
    private String action;
    /** 处置结果，例如 allowed、blocked、recorded。 */
    private String result;
    /** 事件描述。 */
    private String message;
    /** 事件发生时间。 */
    private LocalDateTime occurredAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventLevel() {
        return eventLevel;
    }

    public void setEventLevel(String eventLevel) {
        this.eventLevel = eventLevel;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
