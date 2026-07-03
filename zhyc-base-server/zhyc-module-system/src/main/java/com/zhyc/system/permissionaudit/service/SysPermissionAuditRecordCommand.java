/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.service;

/**
 * 系统权限变更审计记录命令。
 */
public class SysPermissionAuditRecordCommand {

    /** 租户业务编码。 */
    private final String tenantId;
    /** 操作者用户主键。 */
    private final Long operatorId;
    /** 目标类型。 */
    private final String targetType;
    /** 目标业务标识。 */
    private final String targetId;
    /** 变更前内容。 */
    private final String beforeValue;
    /** 变更后内容。 */
    private final String afterValue;
    /** 变更类型。 */
    private final String changeType;

    /**
     * 创建系统权限变更审计记录命令。
     *
     * @param tenantId 租户业务编码
     * @param operatorId 操作者用户主键
     * @param targetType 目标类型
     * @param targetId 目标业务标识
     * @param beforeValue 变更前内容
     * @param afterValue 变更后内容
     * @param changeType 变更类型
     */
    public SysPermissionAuditRecordCommand(String tenantId, Long operatorId, String targetType, String targetId,
                                           String beforeValue, String afterValue, String changeType) {
        this.tenantId = tenantId;
        this.operatorId = operatorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.changeType = changeType;
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
     * 返回操作者用户主键。
     *
     * @return 操作者用户主键
     */
    public Long getOperatorId() {
        return operatorId;
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
     * 返回目标业务标识。
     *
     * @return 目标业务标识
     */
    public String getTargetId() {
        return targetId;
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
     * 返回变更后内容。
     *
     * @return 变更后内容
     */
    public String getAfterValue() {
        return afterValue;
    }

    /**
     * 返回变更类型。
     *
     * @return 变更类型
     */
    public String getChangeType() {
        return changeType;
    }
}
