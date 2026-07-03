/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.service;

import java.time.LocalDateTime;

/**
 * 系统审计日志响应对象。
 */
public class SysAuditLogResponse {

    /** 审计日志主键。 */
    private final Long id;
    /** 租户业务编码。 */
    private final String tenantId;
    /** 操作用户主键。 */
    private final Long userId;
    /** 操作账号。 */
    private final String username;
    /** 操作动作编码。 */
    private final String action;
    /** 被操作目标类型。 */
    private final String targetType;
    /** 被操作目标标识。 */
    private final String targetId;
    /** 操作结果。 */
    private final String result;
    /** 客户端 IP。 */
    private final String clientIp;
    /** 操作详情或失败原因。 */
    private final String detail;
    /** 审计日志创建时间。 */
    private final LocalDateTime createdAt;

    /**
     * 创建系统审计日志响应对象。
     *
     * @param id 审计日志主键
     * @param tenantId 租户业务编码
     * @param userId 操作用户主键
     * @param username 操作账号
     * @param action 操作动作编码
     * @param targetType 被操作目标类型
     * @param targetId 被操作目标标识
     * @param result 操作结果
     * @param clientIp 客户端 IP
     * @param detail 操作详情或失败原因
     * @param createdAt 审计日志创建时间
     */
    public SysAuditLogResponse(Long id, String tenantId, Long userId, String username, String action,
                               String targetType, String targetId, String result, String clientIp,
                               String detail, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.result = result;
        this.clientIp = clientIp;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    /**
     * 返回审计日志主键。
     *
     * @return 审计日志主键
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
     * 返回操作用户主键。
     *
     * @return 操作用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 返回操作账号。
     *
     * @return 操作账号
     */
    public String getUsername() {
        return username;
    }

    /**
     * 返回操作动作编码。
     *
     * @return 操作动作编码
     */
    public String getAction() {
        return action;
    }

    /**
     * 返回被操作目标类型。
     *
     * @return 被操作目标类型
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 返回被操作目标标识。
     *
     * @return 被操作目标标识
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 返回操作结果。
     *
     * @return 操作结果
     */
    public String getResult() {
        return result;
    }

    /**
     * 返回客户端 IP。
     *
     * @return 客户端 IP
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * 返回操作详情或失败原因。
     *
     * @return 操作详情或失败原因
     */
    public String getDetail() {
        return detail;
    }

    /**
     * 返回审计日志创建时间。
     *
     * @return 审计日志创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
