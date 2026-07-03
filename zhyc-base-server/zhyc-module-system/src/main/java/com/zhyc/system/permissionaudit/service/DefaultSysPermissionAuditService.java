/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.service;

import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.permissionaudit.domain.SysPermissionAudit;
import com.zhyc.system.permissionaudit.domain.SysPermissionAuditChangeType;
import com.zhyc.system.permissionaudit.domain.SysPermissionAuditTargetType;
import com.zhyc.system.permissionaudit.repository.SysPermissionAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统权限变更审计业务服务实现。
 */
@Service
public class DefaultSysPermissionAuditService implements SysPermissionAuditService {

    /** 最近权限变更审计最大查询条数。 */
    private static final int MAX_RECENT_LIMIT = 200;
    /** 权限变更审计记录命令不能为空错误码。 */
    private static final String ERROR_COMMAND_REQUIRED = "ZHYC_SYS_PERMISSION_AUDIT_COMMAND_REQUIRED";
    /** 租户业务编码不能为空错误码。 */
    private static final String ERROR_TENANT_REQUIRED = "ZHYC_SYS_PERMISSION_AUDIT_TENANT_REQUIRED";
    /** 目标类型不能为空错误码。 */
    private static final String ERROR_TARGET_TYPE_REQUIRED = "ZHYC_SYS_PERMISSION_AUDIT_TARGET_TYPE_REQUIRED";
    /** 目标业务标识不能为空错误码。 */
    private static final String ERROR_TARGET_ID_REQUIRED = "ZHYC_SYS_PERMISSION_AUDIT_TARGET_ID_REQUIRED";
    /** 变更类型不能为空错误码。 */
    private static final String ERROR_CHANGE_TYPE_REQUIRED = "ZHYC_SYS_PERMISSION_AUDIT_CHANGE_TYPE_REQUIRED";
    /** 权限审计目标类型不支持错误码。 */
    private static final String ERROR_TARGET_TYPE_UNSUPPORTED =
            "ZHYC_SYS_PERMISSION_AUDIT_TARGET_TYPE_UNSUPPORTED";
    /** 权限审计变更类型不支持错误码。 */
    private static final String ERROR_CHANGE_TYPE_UNSUPPORTED =
            "ZHYC_SYS_PERMISSION_AUDIT_CHANGE_TYPE_UNSUPPORTED";

    /** 系统权限变更审计仓储。 */
    private final SysPermissionAuditRepository permissionAuditRepository;

    /**
     * 创建默认系统权限变更审计业务服务。
     *
     * @param permissionAuditRepository 系统权限变更审计仓储
     */
    public DefaultSysPermissionAuditService(SysPermissionAuditRepository permissionAuditRepository) {
        this.permissionAuditRepository = Objects.requireNonNull(permissionAuditRepository, "系统权限变更审计仓储不能为空");
    }

    /**
     * 记录一条权限变更审计，并对租户、目标和变更类型做基础校验。
     *
     * @param command 权限变更审计记录命令
     */
    @Override
    @Transactional
    public void record(SysPermissionAuditRecordCommand command) {
        SysPermissionAuditRecordCommand requiredCommand = requireObject(command, ERROR_COMMAND_REQUIRED,
                "系统权限变更审计记录命令不能为空");
        SysPermissionAudit permissionAudit = new SysPermissionAudit(null,
                requireText(requiredCommand.getTenantId(), ERROR_TENANT_REQUIRED, "租户业务编码不能为空"),
                requiredCommand.getOperatorId(),
                normalizeTargetType(requireText(requiredCommand.getTargetType(), ERROR_TARGET_TYPE_REQUIRED,
                        "目标类型不能为空")),
                requireText(requiredCommand.getTargetId(), ERROR_TARGET_ID_REQUIRED, "目标业务标识不能为空"),
                trimToNull(requiredCommand.getBeforeValue()), trimToNull(requiredCommand.getAfterValue()),
                normalizeChangeType(requireText(requiredCommand.getChangeType(), ERROR_CHANGE_TYPE_REQUIRED,
                        "变更类型不能为空")),
                LocalDateTime.now());
        permissionAuditRepository.save(permissionAudit);
    }

    /**
     * 查询租户最近权限变更审计，并限制最大返回条数。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近权限变更审计列表
     */
    @Override
    public List<SysPermissionAuditResponse> listRecent(String tenantId, int limit) {
        String requiredTenantId = requireText(tenantId, ERROR_TENANT_REQUIRED, "租户业务编码不能为空");
        int boundedLimit = Math.max(1, Math.min(limit, MAX_RECENT_LIMIT));
        return permissionAuditRepository.findRecentByTenantId(requiredTenantId, boundedLimit).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 将领域模型转换为接口响应对象。
     *
     * @param permissionAudit 权限变更审计领域模型
     * @return 权限变更审计响应对象
     */
    private SysPermissionAuditResponse toResponse(SysPermissionAudit permissionAudit) {
        return new SysPermissionAuditResponse(permissionAudit.getId(), permissionAudit.getTenantId(),
                permissionAudit.getOperatorId(), permissionAudit.getTargetType(), permissionAudit.getTargetId(),
                permissionAudit.getBeforeValue(), permissionAudit.getAfterValue(), permissionAudit.getChangeType(),
                permissionAudit.getCreatedAt());
    }

    /**
     * 校验并规范化权限变更审计目标类型。
     *
     * @param targetType 目标类型编码
     * @return 规范化后的目标类型编码
     */
    private String normalizeTargetType(String targetType) {
        try {
            return SysPermissionAuditTargetType.fromCode(targetType).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_TARGET_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验并规范化权限审计变更类型。
     *
     * @param changeType 变更类型编码
     * @return 规范化后的变更类型编码
     */
    private String normalizeChangeType(String changeType) {
        try {
            return SysPermissionAuditChangeType.fromCode(changeType).getCode();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ERROR_CHANGE_TYPE_UNSUPPORTED, ex.getMessage());
        }
    }

    /**
     * 校验业务对象不能为空。
     *
     * @param value 原始对象
     * @param code 业务错误码
     * @param message 为空时的异常消息
     * @return 校验后的对象
     * @param <T> 对象类型
     */
    private <T> T requireObject(T value, String code, String message) {
        if (value == null) {
            throw new BusinessException(code, message);
        }
        return value;
    }

    /**
     * 校验文本字段不能为空，并返回去除首尾空白后的值。
     *
     * @param value 待校验文本
     * @param message 校验失败提示
     * @return 标准化后的文本
     */
    private String requireText(String value, String code, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(code, message);
        }
        return normalized;
    }

    /**
     * 去除文本首尾空白，空文本统一转换为 {@code null}。
     *
     * @param value 原始文本
     * @return 标准化后的文本
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
