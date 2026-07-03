/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.mapper;

import com.zhyc.system.audit.domain.SysAuditLog;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统审计日志 MyBatis Mapper。
 */
@Mapper
public interface SysAuditLogMapper {

    /**
     * 新增系统审计日志。
     *
     * @param auditLog 系统审计日志
     */
    @InsertProvider(type = SysAuditLogSqlProvider.class, method = "insert")
    void insert(SysAuditLog auditLog);

    /**
     * 查询租户最近审计日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近审计日志列表
     */
    @SelectProvider(type = SysAuditLogSqlProvider.class, method = "selectRecentByTenantId")
    List<SysAuditLog> selectRecentByTenantId(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
