/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.service;

import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.system.permission.repository.SysPermissionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 默认系统权限业务服务实现。
 */
@Service
public class DefaultSysPermissionService implements SysPermissionService {

    /** 系统权限仓储。 */
    private final SysPermissionRepository permissionRepository;

    /**
     * 创建默认系统权限业务服务。
     *
     * @param permissionRepository 系统权限仓储
     */
    public DefaultSysPermissionService(SysPermissionRepository permissionRepository) {
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "系统权限仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.SYS_USER_PERMISSIONS,
            key = "(#tenantId == null ? '' : #tenantId.trim()) + ':' + #userId")
    public List<String> listUserPermissions(String tenantId, Long userId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = Objects.requireNonNull(userId, "用户主键不能为空");
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        permissionRepository.findGrantedPermissions(requiredTenantId, requiredUserId).stream()
                .filter(permission -> permission != null && !permission.trim().isEmpty())
                .map(String::trim)
                .forEach(permissions::add);
        return List.copyOf(permissions);
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }
}
