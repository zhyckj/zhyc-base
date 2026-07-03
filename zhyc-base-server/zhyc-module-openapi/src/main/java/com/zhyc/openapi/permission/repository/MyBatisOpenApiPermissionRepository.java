/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.repository;

import com.zhyc.openapi.permission.domain.OpenApiPermission;
import com.zhyc.openapi.permission.mapper.OpenApiPermissionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的开放 API 权限授权仓储实现。
 */
@Repository
public class MyBatisOpenApiPermissionRepository implements OpenApiPermissionRepository {

    /** 开放 API 权限授权 Mapper。 */
    private final OpenApiPermissionMapper permissionMapper;

    /**
     * 创建开放 API 权限授权仓储实现。
     *
     * @param permissionMapper 开放 API 权限授权 Mapper
     */
    public MyBatisOpenApiPermissionRepository(OpenApiPermissionMapper permissionMapper) {
        this.permissionMapper = Objects.requireNonNull(permissionMapper, "开放 API 权限授权 Mapper 不能为空");
    }

    @Override
    public List<OpenApiPermission> findByTenantIdAndAppCode(String tenantId, String appCode) {
        return permissionMapper.selectByTenantIdAndAppCode(tenantId, appCode);
    }

    @Override
    public void save(OpenApiPermission permission) {
        permissionMapper.upsert(permission);
    }
}
