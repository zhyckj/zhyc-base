/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.repository;

import com.zhyc.system.audit.domain.SysAuditLog;
import com.zhyc.system.audit.mapper.SysAuditLogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统审计日志仓储实现。
 */
@Repository
public class MyBatisSysAuditLogRepository implements SysAuditLogRepository {

    /** 系统审计日志 Mapper。 */
    private final SysAuditLogMapper auditLogMapper;

    /**
     * 创建系统审计日志仓储实现。
     *
     * @param auditLogMapper 系统审计日志 Mapper
     */
    public MyBatisSysAuditLogRepository(SysAuditLogMapper auditLogMapper) {
        this.auditLogMapper = Objects.requireNonNull(auditLogMapper, "系统审计日志 Mapper 不能为空");
    }

    @Override
    public void save(SysAuditLog auditLog) {
        auditLogMapper.insert(auditLog);
    }

    @Override
    public List<SysAuditLog> findRecentByTenantId(String tenantId, int limit) {
        return auditLogMapper.selectRecentByTenantId(tenantId, limit);
    }
}
