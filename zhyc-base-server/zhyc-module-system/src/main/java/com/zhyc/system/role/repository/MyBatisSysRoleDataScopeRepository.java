/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.repository;

import com.zhyc.system.role.domain.SysRoleDataScope;
import com.zhyc.system.role.mapper.SysRoleDataScopeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统角色自定义数据权限仓储实现。
 */
@Repository
public class MyBatisSysRoleDataScopeRepository implements SysRoleDataScopeRepository {

    /** 系统角色自定义数据权限 Mapper。 */
    private final SysRoleDataScopeMapper roleDataScopeMapper;

    /**
     * 创建系统角色自定义数据权限仓储实现。
     *
     * @param roleDataScopeMapper 系统角色自定义数据权限 Mapper
     */
    public MyBatisSysRoleDataScopeRepository(SysRoleDataScopeMapper roleDataScopeMapper) {
        this.roleDataScopeMapper = Objects.requireNonNull(roleDataScopeMapper,
                "系统角色自定义数据权限 Mapper 不能为空");
    }

    @Override
    public List<SysRoleDataScope> findByTenantIdAndRoleId(String tenantId, Long roleId) {
        return roleDataScopeMapper.selectByTenantIdAndRoleId(tenantId, roleId);
    }

    @Override
    public void replaceRoleDataScopes(String tenantId, Long roleId, List<Long> orgIds) {
        roleDataScopeMapper.deleteByTenantIdAndRoleId(tenantId, roleId);
        for (Long orgId : orgIds) {
            roleDataScopeMapper.insertRoleDataScope(tenantId, roleId, orgId, "org");
        }
    }
}
