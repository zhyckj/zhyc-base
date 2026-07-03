/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.repository;

import com.zhyc.system.adminscope.domain.SysAdminScope;
import com.zhyc.system.adminscope.mapper.SysAdminScopeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统管理员管理范围仓储实现。
 */
@Repository
public class MyBatisSysAdminScopeRepository implements SysAdminScopeRepository {

    /** 系统管理员管理范围 Mapper。 */
    private final SysAdminScopeMapper adminScopeMapper;

    /**
     * 创建系统管理员管理范围仓储实现。
     *
     * @param adminScopeMapper 系统管理员管理范围 Mapper
     */
    public MyBatisSysAdminScopeRepository(SysAdminScopeMapper adminScopeMapper) {
        this.adminScopeMapper = Objects.requireNonNull(adminScopeMapper, "系统管理员管理范围 Mapper 不能为空");
    }

    @Override
    public List<SysAdminScope> findByTenantIdAndUserId(String tenantId, Long userId) {
        return adminScopeMapper.selectByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public void replaceAdminScopes(String tenantId, Long userId, List<SysAdminScope> scopes) {
        adminScopeMapper.deleteByTenantIdAndUserId(tenantId, userId);
        for (SysAdminScope scope : scopes) {
            adminScopeMapper.insertAdminScope(tenantId, userId, scope.getScopeType(), scope.getScopeRefCode());
        }
    }
}
