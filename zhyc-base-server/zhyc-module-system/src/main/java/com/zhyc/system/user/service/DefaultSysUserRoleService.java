/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.repository.SysRoleRepository;
import com.zhyc.system.user.domain.SysUserRole;
import com.zhyc.system.user.repository.SysUserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认系统用户角色业务服务实现。
 */
@Service
public class DefaultSysUserRoleService implements SysUserRoleService {

    /** 系统用户角色仓储。 */
    private final SysUserRoleRepository userRoleRepository;
    /** 系统角色仓储，用于校验角色是否属于当前租户。 */
    private final SysRoleRepository roleRepository;

    /**
     * 创建默认系统用户角色业务服务。
     *
     * @param userRoleRepository 系统用户角色仓储
     * @param roleRepository 系统角色仓储
     */
    public DefaultSysUserRoleService(SysUserRoleRepository userRoleRepository, SysRoleRepository roleRepository) {
        this.userRoleRepository = Objects.requireNonNull(userRoleRepository, "系统用户角色仓储不能为空");
        this.roleRepository = Objects.requireNonNull(roleRepository, "系统角色仓储不能为空");
    }

    @Override
    public List<SysUserRoleResponse> listUserRoles(String tenantId, Long userId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredUserId = requirePositive(userId, "用户主键不能为空");
        return userRoleRepository.findByTenantIdAndUserId(requiredTenantId, requiredUserId).stream()
                .sorted(Comparator.comparing(SysUserRole::getRoleId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void bindUserRoles(SysUserRoleBindCommand command) {
        Objects.requireNonNull(command, "用户角色绑定命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        Long requiredUserId = requirePositive(command.getUserId(), "用户主键不能为空");
        Map<Long, SysUserRole> bindings = new LinkedHashMap<>();
        if (command.getRoleIds() != null) {
            for (Long roleId : command.getRoleIds()) {
                if (roleId == null || roleId <= 0 || bindings.containsKey(roleId)) {
                    continue;
                }
                bindings.put(roleId, new SysUserRole(null, requiredTenantId, requiredUserId, roleId,
                        null, null, null, null, null));
            }
        }
        validateTenantRoles(requiredTenantId, bindings.keySet());
        userRoleRepository.replaceUserRoles(requiredTenantId, requiredUserId, List.copyOf(bindings.values()));
    }

    /**
     * 校验待绑定角色均属于当前租户。
     *
     * <p>必须在替换用户角色绑定前完成，避免非法角色导致旧授权被提前清空。</p>
     *
     * @param tenantId 租户业务编码
     * @param roleIds 待绑定角色主键集合
     */
    private void validateTenantRoles(String tenantId, Set<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        Set<Long> tenantRoleIds = roleRepository.findByTenantId(tenantId).stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long roleId : roleIds) {
            if (!tenantRoleIds.contains(roleId)) {
                throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色主键不属于当前租户：" + roleId);
            }
        }
    }

    private SysUserRoleResponse toResponse(SysUserRole userRole) {
        return new SysUserRoleResponse(userRole.getRoleId(), userRole.getRoleCode(), userRole.getRoleName(),
                userRole.getDataScope(), userRole.getStatus());
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value.trim();
    }

    private Long requirePositive(Long value, String message) {
        if (value == null || value <= 0) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure(message);
        }
        return value;
    }
}
