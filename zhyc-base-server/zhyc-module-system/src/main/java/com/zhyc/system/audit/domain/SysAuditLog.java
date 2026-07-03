/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.domain;

import java.time.LocalDateTime;

/**
 * 系统审计日志领域模型。
 *
 * <p>审计日志按 {@code tenantId} 做租户隔离，用于记录后台关键操作和安全审计事件。</p>
 */
public class SysAuditLog {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 操作用户主键，匿名或系统任务可为空。 */
    private Long userId;
    /** 操作账号，便于审计页面直接展示。 */
    private String username;
    /** 操作动作编码，例如 ROLE_BIND_MENU。 */
    private String action;
    /** 被操作目标类型，例如 sys_role。 */
    private String targetType;
    /** 被操作目标标识。 */
    private String targetId;
    /** 操作结果，例如 success、failure。 */
    private String result;
    /** 客户端 IP。 */
    private String clientIp;
    /** 操作详情或失败原因。 */
    private String detail;
    /** 审计日志创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空审计日志对象。
     */
    public SysAuditLog() {
    }

    /**
     * 创建完整审计日志对象。
     *
     * @param id 数据库主键
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
    public SysAuditLog(Long id, String tenantId, Long userId, String username, String action, String targetType,
                       String targetId, String result, String clientIp, String detail, LocalDateTime createdAt) {
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
     * 返回数据库主键。
     *
     * @return 数据库主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置数据库主键。
     *
     * @param id 数据库主键
     */
    public void setId(Long id) {
        this.id = id;
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
     * 设置租户业务编码。
     *
     * @param tenantId 租户业务编码
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
     * 设置操作用户主键。
     *
     * @param userId 操作用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * 设置操作账号。
     *
     * @param username 操作账号
     */
    public void setUsername(String username) {
        this.username = username;
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
     * 设置操作动作编码。
     *
     * @param action 操作动作编码
     */
    public void setAction(String action) {
        this.action = action;
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
     * 设置被操作目标类型。
     *
     * @param targetType 被操作目标类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
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
     * 设置被操作目标标识。
     *
     * @param targetId 被操作目标标识
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
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
     * 设置操作结果。
     *
     * @param result 操作结果
     */
    public void setResult(String result) {
        this.result = result;
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
     * 设置客户端 IP。
     *
     * @param clientIp 客户端 IP
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
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
     * 设置操作详情或失败原因。
     *
     * @param detail 操作详情或失败原因
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * 返回审计日志创建时间。
     *
     * @return 审计日志创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置审计日志创建时间。
     *
     * @param createdAt 审计日志创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
