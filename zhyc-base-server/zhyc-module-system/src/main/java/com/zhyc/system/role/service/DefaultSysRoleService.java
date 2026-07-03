/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import com.zhyc.system.menu.domain.SysMenu;
import com.zhyc.system.menu.repository.SysMenuRepository;
import com.zhyc.system.permission.DataScope;
import com.zhyc.system.role.domain.SysRole;
import com.zhyc.system.role.repository.SysRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认系统角色业务服务实现。
 */
@Service
public class DefaultSysRoleService implements SysRoleService {

    /** 系统角色仓储。 */
    private final SysRoleRepository roleRepository;
    /** 系统菜单仓储，用于校验菜单是否属于当前租户。 */
    private final SysMenuRepository menuRepository;

    /**
     * 创建默认系统角色业务服务。
     *
     * @param roleRepository 系统角色仓储
     * @param menuRepository 系统菜单仓储
     */
    public DefaultSysRoleService(SysRoleRepository roleRepository, SysMenuRepository menuRepository) {
        this.roleRepository = Objects.requireNonNull(roleRepository, "系统角色仓储不能为空");
        this.menuRepository = Objects.requireNonNull(menuRepository, "系统菜单仓储不能为空");
    }

    @Override
    public List<SysRoleResponse> listRoles(String tenantId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        return roleRepository.findByTenantId(requiredTenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void saveRole(SysRoleSaveCommand command) {
        Objects.requireNonNull(command, "角色保存命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        String requiredRoleCode = requireText(command.getRoleCode(), "角色编码不能为空");
        String requiredName = requireText(command.getName(), "角色名称不能为空");
        DataScope requiredDataScope = parseDataScope(command.getDataScope());
        String requiredStatus = normalizeStatus(command.getStatus());
        SysRole role = new SysRole();
        role.setId(command.getRoleId());
        role.setTenantId(requiredTenantId);
        role.setRoleCode(requiredRoleCode);
        role.setName(requiredName);
        role.setDataScope(requiredDataScope);
        role.setStatus(requiredStatus);
        if (command.getRoleId() == null) {
            roleRepository.insert(role);
            return;
        }
        Long requiredRoleId = requirePositive(command.getRoleId(), "角色主键不能为空");
        validateTenantRole(requiredTenantId, requiredRoleId);
        roleRepository.update(role);
    }

    @Override
    @Transactional
    public void updateStatus(String tenantId, Long roleId, String status) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(roleId, "角色主键不能为空");
        String requiredStatus = normalizeStatus(status);
        validateTenantRole(requiredTenantId, requiredRoleId);
        roleRepository.updateStatus(requiredTenantId, requiredRoleId, requiredStatus);
    }

    @Override
    @Transactional
    public void deleteRole(String tenantId, Long roleId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(roleId, "角色主键不能为空");
        validateTenantRole(requiredTenantId, requiredRoleId);
        roleRepository.deleteByTenantIdAndId(requiredTenantId, requiredRoleId);
    }

    @Override
    public List<Long> listRoleMenuIds(String tenantId, Long roleId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(roleId, "角色主键不能为空");
        validateTenantRole(requiredTenantId, requiredRoleId);
        return roleRepository.findRoleMenuIds(requiredTenantId, requiredRoleId);
    }

    @Override
    @Transactional
    public void bindRoleMenus(RoleMenuBindCommand command) {
        Objects.requireNonNull(command, "角色菜单绑定命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        Long requiredRoleId = requirePositive(command.getRoleId(), "角色主键不能为空");
        LinkedHashSet<Long> menuIds = new LinkedHashSet<>();
        if (command.getMenuIds() != null) {
            command.getMenuIds().stream()
                    .filter(menuId -> menuId != null && menuId > 0)
                    .forEach(menuIds::add);
        }
        validateTenantRole(requiredTenantId, requiredRoleId);
        validateTenantMenus(requiredTenantId, menuIds);
        roleRepository.replaceRoleMenus(requiredTenantId, requiredRoleId, List.copyOf(menuIds));
    }

    /**
     * 校验角色属于当前租户。
     *
     * <p>角色菜单绑定必须先确认角色归属，避免跨租户角色授权。</p>
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
     * 校验待绑定菜单均属于当前租户。
     *
     * <p>必须在替换角色菜单绑定前完成，避免非法菜单导致旧授权被提前清空。</p>
     *
     * @param tenantId 租户业务编码
     * @param menuIds 待绑定菜单主键集合
     */
    private void validateTenantMenus(String tenantId, Set<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        Set<Long> tenantMenuIds = menuRepository.findEnabledByTenantId(tenantId).stream()
                .map(SysMenu::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long menuId : menuIds) {
            if (!tenantMenuIds.contains(menuId)) {
                throw com.zhyc.system.support.SystemServiceValidation.businessFailure("菜单主键不属于当前租户：" + menuId);
            }
        }
    }

    private SysRoleResponse toResponse(SysRole role) {
        return new SysRoleResponse(role.getId(), role.getTenantId(), role.getRoleCode(), role.getName(),
                role.getDataScope(), role.getStatus());
    }

    /**
     * 解析数据权限范围。
     *
     * @param dataScope 数据权限范围文本
     * @return 数据权限范围枚举
     */
    private DataScope parseDataScope(String dataScope) {
        String normalizedDataScope = requireText(dataScope, "数据权限范围不能为空").toUpperCase();
        try {
            return DataScope.valueOf(normalizedDataScope);
        } catch (IllegalArgumentException ex) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("不支持的数据权限范围：" + dataScope);
        }
    }

    /**
     * 规范化状态值。
     *
     * @param status 状态文本
     * @return 状态值
     */
    private String normalizeStatus(String status) {
        String normalizedStatus = requireText(status, "角色状态不能为空").toLowerCase();
        if (!"enabled".equals(normalizedStatus) && !"disabled".equals(normalizedStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("角色状态仅支持 enabled 或 disabled");
        }
        return normalizedStatus;
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
