/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.repository;

import com.zhyc.system.permission.mapper.SysPermissionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统权限仓储实现。
 */
@Repository
public class MyBatisSysPermissionRepository implements SysPermissionRepository {

    /** 系统权限 Mapper。 */
    private final SysPermissionMapper permissionMapper;

    /**
     * 创建系统权限仓储实现。
     *
     * @param permissionMapper 系统权限 Mapper
     */
    public MyBatisSysPermissionRepository(SysPermissionMapper permissionMapper) {
        this.permissionMapper = Objects.requireNonNull(permissionMapper, "系统权限 Mapper 不能为空");
    }

    @Override
    public List<String> findGrantedPermissions(String tenantId, Long userId) {
        return permissionMapper.selectPermissionsByTenantAndUser(tenantId, userId);
    }
}
