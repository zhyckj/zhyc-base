/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.domain;

import java.time.LocalDateTime;

/**
 * 系统权限变更审计领域模型。
 *
 * <p>权限变更审计按 {@code tenantId} 做租户隔离，用于记录角色授权、菜单授权和数据权限调整。</p>
 */
public class SysPermissionAudit {

    /** 数据库主键。 */
    private Long id;
    /** 租户业务编码。 */
    private String tenantId;
    /** 操作者用户主键。 */
    private Long operatorId;
    /** 目标类型，例如 role、user、menu。 */
    private String targetType;
    /** 目标业务标识。 */
    private String targetId;
    /** 变更前内容。 */
    private String beforeValue;
    /** 变更后内容。 */
    private String afterValue;
    /** 变更类型。 */
    private String changeType;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    /**
     * 创建空权限变更审计对象。
     */
    public SysPermissionAudit() {
    }

    /**
     * 创建完整权限变更审计对象。
     *
     * @param id 数据库主键
     * @param tenantId 租户业务编码
     * @param operatorId 操作者用户主键
     * @param targetType 目标类型
     * @param targetId 目标业务标识
     * @param beforeValue 变更前内容
     * @param afterValue 变更后内容
     * @param changeType 变更类型
     * @param createdAt 创建时间
     */
    public SysPermissionAudit(Long id, String tenantId, Long operatorId, String targetType, String targetId,
                              String beforeValue, String afterValue, String changeType, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.operatorId = operatorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.changeType = changeType;
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
     * 返回操作者用户主键。
     *
     * @return 操作者用户主键
     */
    public Long getOperatorId() {
        return operatorId;
    }

    /**
     * 设置操作者用户主键。
     *
     * @param operatorId 操作者用户主键
     */
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * 返回目标类型。
     *
     * @return 目标类型
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 设置目标类型。
     *
     * @param targetType 目标类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * 返回目标业务标识。
     *
     * @return 目标业务标识
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 设置目标业务标识。
     *
     * @param targetId 目标业务标识
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * 返回变更前内容。
     *
     * @return 变更前内容
     */
    public String getBeforeValue() {
        return beforeValue;
    }

    /**
     * 设置变更前内容。
     *
     * @param beforeValue 变更前内容
     */
    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    /**
     * 返回变更后内容。
     *
     * @return 变更后内容
     */
    public String getAfterValue() {
        return afterValue;
    }

    /**
     * 设置变更后内容。
     *
     * @param afterValue 变更后内容
     */
    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }

    /**
     * 返回变更类型。
     *
     * @return 变更类型
     */
    public String getChangeType() {
        return changeType;
    }

    /**
     * 设置变更类型。
     *
     * @param changeType 变更类型
     */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
