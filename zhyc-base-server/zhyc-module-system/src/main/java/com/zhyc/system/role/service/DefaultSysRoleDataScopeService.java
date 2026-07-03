/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import com.zhyc.system.org.domain.SysOrg;
import com.zhyc.system.org.repository.SysOrgRepository;
import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.domain.SysRoleDataScope;
import com.zhyc.system.role.repository.SysRoleRepository;
import com.zhyc.system.role.repository.SysRoleDataScopeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认系统角色自定义数据权限业务服务实现。
 */
@Service
public class DefaultSysRoleDataScopeService implements SysRoleDataScopeService {

    /** 系统角色自定义数据权限仓储。 */
    private final SysRoleDataScopeRepository roleDataScopeRepository;
    /** 系统角色仓储，用于校验角色是否属于当前租户。 */
    private final SysRoleRepository roleRepository;
    /** 系统组织仓储，用于校验组织是否属于当前租户。 */
    private final SysOrgRepository orgRepository;

    /**
     * 创建默认系统角色自定义数据权限业务服务。
     *
     * @param roleDataScopeRepository 系统角色自定义数据权限仓储
     * @param roleRepository 系统角色仓储
     * @param orgRepository 系统组织仓储
     */
    public DefaultSysRoleDataScopeService(SysRoleDataScopeRepository roleDataScopeRepository,
                                          SysRoleRepository roleRepository,
                                          SysOrgRepository orgRepository) {
        this.roleDataScopeRepository = Objects.requireNonNull(roleDataScopeRepository,
                "系统角色自定义数据权限仓储不能为空");
        this.roleRepository = Objects.requireNonNull(roleRepository, "系统角色仓储不能为空");
        this.orgRepository = Objects.requireNonNull(orgRepository, "系统组织仓储不能为空");
    }

    @Override
    public List<SysRoleDataScopeResponse> listRoleDataScopes(String tenantId, Long roleId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(roleId, "角色主键不能为空");
        return roleDataScopeRepository.findByTenantIdAndRoleId(requiredTenantId, requiredRoleId).stream()
                .sorted(Comparator.comparing(SysRoleDataScope::getOrgId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void bindRoleDataScopes(RoleDataScopeBindCommand command) {
        Objects.requireNonNull(command, "角色自定义数据权限绑定命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(command.getRoleId(), "角色主键不能为空");
        LinkedHashSet<Long> orgIds = new LinkedHashSet<>();
        if (command.getOrgIds() != null) {
            command.getOrgIds().stream()
                    .filter(orgId -> orgId != null && orgId > 0)
                    .forEach(orgIds::add);
        }
        validateTenantRole(requiredTenantId, requiredRoleId);
        validateTenantOrgs(requiredTenantId, orgIds);
        roleDataScopeRepository.replaceRoleDataScopes(requiredTenantId, requiredRoleId, List.copyOf(orgIds));
    }

    /**
     * 校验角色属于当前租户。
     *
     * <p>角色数据权限绑定必须先确认角色归属，避免跨租户角色被授予组织范围。</p>
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    private void validateTenantRole(String tenantId, Long roleId) {
        boolean exists = roleRepository.findByTenantId(tenantId).stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .anyMatch(roleId::equals);
        if (!exists) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色主键不属于当前租户：" + roleId);
        }
    }

    /**
     * 校验待绑定组织均属于当前租户。
     *
     * <p>必须在替换角色数据权限前完成，避免非法组织导致旧数据权限被提前清空。</p>
     *
     * @param tenantId 租户业务编码
     * @param orgIds 待绑定组织主键集合
     */
    private void validateTenantOrgs(String tenantId, Set<Long> orgIds) {
        if (orgIds.isEmpty()) {
            return;
        }
        Set<Long> tenantOrgIds = orgRepository.findByTenantId(tenantId).stream()
                .map(SysOrg::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long orgId : orgIds) {
            if (!tenantOrgIds.contains(orgId)) {
                throw com.zhyc.system.support.SystemServiceValidation.businessFailure("组织主键不属于当前租户：" + orgId);
            }
        }
    }

    private SysRoleDataScopeResponse toResponse(SysRoleDataScope dataScope) {
        return new SysRoleDataScopeResponse(dataScope.getOrgId(), dataScope.getOrgName(), dataScope.getScopeType());
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
