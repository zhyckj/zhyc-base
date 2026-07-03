/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.mapper;

import com.zhyc.system.permissionaudit.domain.SysPermissionAudit;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统权限变更审计 MyBatis Mapper。
 */
@Mapper
public interface SysPermissionAuditMapper {

    /**
     * 新增系统权限变更审计。
     *
     * @param permissionAudit 系统权限变更审计
     */
    @InsertProvider(type = SysPermissionAuditSqlProvider.class, method = "insert")
    void insert(SysPermissionAudit permissionAudit);

    /**
     * 查询租户最近权限变更审计。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近权限变更审计列表
     */
    @SelectProvider(type = SysPermissionAuditSqlProvider.class, method = "selectRecentByTenantId")
    List<SysPermissionAudit> selectRecentByTenantId(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
