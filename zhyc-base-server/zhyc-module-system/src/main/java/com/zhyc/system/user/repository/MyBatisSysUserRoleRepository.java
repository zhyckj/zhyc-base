/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.repository;

import com.zhyc.system.user.domain.SysUserRole;
import com.zhyc.system.user.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统用户角色仓储实现。
 */
@Repository
public class MyBatisSysUserRoleRepository implements SysUserRoleRepository {

    /** 系统用户角色 Mapper。 */
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 创建系统用户角色仓储实现。
     *
     * @param userRoleMapper 系统用户角色 Mapper
     */
    public MyBatisSysUserRoleRepository(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = Objects.requireNonNull(userRoleMapper, "系统用户角色 Mapper 不能为空");
    }

    @Override
    public List<SysUserRole> findByTenantIdAndUserId(String tenantId, Long userId) {
        return userRoleMapper.selectByTenantIdAndUserId(tenantId, userId);
    }

    @Override
    public void replaceUserRoles(String tenantId, Long userId, List<SysUserRole> bindings) {
        userRoleMapper.deleteByTenantIdAndUserId(tenantId, userId);
        for (SysUserRole binding : bindings) {
            userRoleMapper.insertUserRole(tenantId, userId, binding.getRoleId());
        }
    }
}
