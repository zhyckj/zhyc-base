/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.service;

import com.zhyc.common.audit.AuditEvent;
import com.zhyc.system.audit.domain.SysAuditLog;
import com.zhyc.system.audit.repository.SysAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统审计日志业务服务实现。
 */
@Service
public class DefaultSysAuditLogService implements SysAuditLogService {

    /** 操作成功结果编码。 */
    private static final String RESULT_SUCCESS = "success";
    /** 操作失败结果编码。 */
    private static final String RESULT_FAILURE = "failure";
    /** 最近日志最大查询条数，避免后台页面一次性拉取过多数据。 */
    private static final int MAX_RECENT_LIMIT = 200;

    /** 系统审计日志仓储。 */
    private final SysAuditLogRepository auditLogRepository;

    /**
     * 创建默认系统审计日志业务服务。
     *
     * @param auditLogRepository 系统审计日志仓储
     */
    public DefaultSysAuditLogService(SysAuditLogRepository auditLogRepository) {
        this.auditLogRepository = Objects.requireNonNull(auditLogRepository, "系统审计日志仓储不能为空");
    }

    @Override
    @Transactional
    public void record(AuditEvent event) {
        Objects.requireNonNull(event, "审计事件不能为空");
        String tenantId = requireText(event.getTenantId(), "租户业务编码不能为空");
        ResourceParts resourceParts = parseResource(event.getResource());
        SysAuditLog auditLog = new SysAuditLog(null, tenantId, parseUserId(event.getUserId()), null,
                requireText(event.getOperation(), "操作动作不能为空"), resourceParts.targetType(),
                resourceParts.targetId(), event.isSuccess() ? RESULT_SUCCESS : RESULT_FAILURE, null,
                trimToNull(event.getMessage()), toCreatedAt(event.getTimestamp()));
        auditLogRepository.save(auditLog);
    }

    @Override
    public List<SysAuditLogResponse> listRecent(String tenantId, int limit) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        int boundedLimit = Math.max(1, Math.min(limit, MAX_RECENT_LIMIT));
        return auditLogRepository.findRecentByTenantId(requiredTenantId, boundedLimit).stream()
                .map(this::toResponse)
                .toList();
    }

    private SysAuditLogResponse toResponse(SysAuditLog auditLog) {
        return new SysAuditLogResponse(auditLog.getId(), auditLog.getTenantId(), auditLog.getUserId(),
                auditLog.getUsername(), auditLog.getAction(), auditLog.getTargetType(), auditLog.getTargetId(),
                auditLog.getResult(), auditLog.getClientIp(), auditLog.getDetail(), auditLog.getCreatedAt());
    }

    private ResourceParts parseResource(String resource) {
        String normalized = trimToNull(resource);
        if (normalized == null) {
            return new ResourceParts(null, null);
        }
        int separatorIndex = normalized.indexOf(':');
        if (separatorIndex < 0) {
            return new ResourceParts(normalized, null);
        }
        String targetType = trimToNull(normalized.substring(0, separatorIndex));
        String targetId = trimToNull(normalized.substring(separatorIndex + 1));
        return new ResourceParts(targetType, targetId);
    }

    private Long parseUserId(String userId) {
        String normalized = trimToNull(userId);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private LocalDateTime toCreatedAt(long timestamp) {
        if (timestamp <= 0) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 审计资源解析结果。
     *
     * @param targetType 被操作目标类型
     * @param targetId 被操作目标标识
     */
    private record ResourceParts(String targetType, String targetId) {
    }
}
