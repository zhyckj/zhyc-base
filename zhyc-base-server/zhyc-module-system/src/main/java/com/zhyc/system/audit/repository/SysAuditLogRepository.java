/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.repository;

import com.zhyc.system.audit.domain.SysAuditLog;

import java.util.List;

/**
 * 系统审计日志仓储。
 */
public interface SysAuditLogRepository {

    /**
     * 保存系统审计日志。
     *
     * @param auditLog 系统审计日志
     */
    void save(SysAuditLog auditLog);

    /**
     * 查询租户最近审计日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近审计日志列表
     */
    List<SysAuditLog> findRecentByTenantId(String tenantId, int limit);
}
