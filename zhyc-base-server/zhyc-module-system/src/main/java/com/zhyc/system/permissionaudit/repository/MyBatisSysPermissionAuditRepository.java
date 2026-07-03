/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permissionaudit.repository;

import com.zhyc.system.permissionaudit.domain.SysPermissionAudit;
import com.zhyc.system.permissionaudit.mapper.SysPermissionAuditMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统权限变更审计仓储实现。
 */
@Repository
public class MyBatisSysPermissionAuditRepository implements SysPermissionAuditRepository {

    /** 系统权限变更审计 Mapper。 */
    private final SysPermissionAuditMapper permissionAuditMapper;

    /**
     * 创建系统权限变更审计仓储实现。
     *
     * @param permissionAuditMapper 系统权限变更审计 Mapper
     */
    public MyBatisSysPermissionAuditRepository(SysPermissionAuditMapper permissionAuditMapper) {
        this.permissionAuditMapper = Objects.requireNonNull(permissionAuditMapper, "系统权限变更审计 Mapper 不能为空");
    }

    /**
     * 保存系统权限变更审计。
     *
     * @param permissionAudit 系统权限变更审计
     */
    @Override
    public void save(SysPermissionAudit permissionAudit) {
        permissionAuditMapper.insert(permissionAudit);
    }

    /**
     * 查询租户最近权限变更审计。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近权限变更审计列表
     */
    @Override
    public List<SysPermissionAudit> findRecentByTenantId(String tenantId, int limit) {
        return permissionAuditMapper.selectRecentByTenantId(tenantId, limit);
    }
}
