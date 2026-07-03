/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.repository;

import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.mapper.SysRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统角色仓储实现。
 */
@Repository
public class MyBatisSysRoleRepository implements SysRoleRepository {

    /** 系统角色 Mapper。 */
    private final SysRoleMapper roleMapper;

    /**
     * 创建系统角色仓储实现。
     *
     * @param roleMapper 系统角色 Mapper
     */
    public MyBatisSysRoleRepository(SysRoleMapper roleMapper) {
        this.roleMapper = Objects.requireNonNull(roleMapper, "系统角色 Mapper 不能为空");
    }

    @Override
    public List<SysRole> findByTenantId(String tenantId) {
        return roleMapper.selectByTenantId(tenantId);
    }

    @Override
    public void insert(SysRole role) {
        roleMapper.insert(role);
    }

    @Override
    public void update(SysRole role) {
        roleMapper.update(role);
    }

    @Override
    public void updateStatus(String tenantId, Long roleId, String status) {
        roleMapper.updateStatus(tenantId, roleId, status);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long roleId) {
        roleMapper.deleteRoleMenusByTenantAndRole(tenantId, roleId);
        roleMapper.deleteRoleDataScopesByTenantAndRole(tenantId, roleId);
        roleMapper.deleteUserRolesByTenantAndRole(tenantId, roleId);
        roleMapper.deleteByTenantIdAndId(tenantId, roleId);
    }

    @Override
    public List<Long> findRoleMenuIds(String tenantId, Long roleId) {
        return roleMapper.selectRoleMenuIdsByTenantAndRole(tenantId, roleId);
    }

    @Override
    public void replaceRoleMenus(String tenantId, Long roleId, List<Long> menuIds) {
        roleMapper.deleteRoleMenusByTenantAndRole(tenantId, roleId);
        for (Long menuId : menuIds) {
            roleMapper.insertRoleMenu(tenantId, roleId, menuId);
        }
    }
}
