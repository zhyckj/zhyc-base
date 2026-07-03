/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.service;

import com.zhyc.common.cache.ZhycCacheNames;
import com.zhyc.system.menu.domain.SysMenu;
import com.zhyc.system.menu.repository.SysMenuRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 默认系统菜单业务服务实现。
 */
@Service
public class DefaultSysMenuService implements SysMenuService {

    /** 系统菜单仓储。 */
    private final SysMenuRepository menuRepository;

    /**
     * 创建默认系统菜单业务服务。
     *
     * @param menuRepository 系统菜单仓储
     */
    public DefaultSysMenuService(SysMenuRepository menuRepository) {
        this.menuRepository = Objects.requireNonNull(menuRepository, "系统菜单仓储不能为空");
    }

    @Override
    @Cacheable(cacheNames = ZhycCacheNames.SYS_MENU_TREE,
            key = "'v2:' + (#tenantId == null ? '' : #tenantId.trim()) + ':' + #includeDisabled")
    public List<SysMenuTreeNode> listMenuTree(String tenantId, boolean includeDisabled) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        List<SysMenu> sourceMenus = includeDisabled
                ? menuRepository.findByTenantId(requiredTenantId)
                : menuRepository.findEnabledByTenantId(requiredTenantId);
        List<SysMenu> menus = sourceMenus.stream()
                .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
        Map<Long, SysMenuTreeNode> nodeMap = new LinkedHashMap<>();
        menus.forEach(menu -> nodeMap.put(menu.getId(), SysMenuTreeNode.from(menu)));

        List<SysMenuTreeNode> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            SysMenuTreeNode node = nodeMap.get(menu.getId());
            SysMenuTreeNode parent = nodeMap.get(menu.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.addChild(node);
        }
        return roots;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.SYS_MENU_TREE, ZhycCacheNames.SYS_USER_PERMISSIONS},
            allEntries = true)
    public void saveMenu(SysMenuSaveCommand command) {
        Objects.requireNonNull(command, "菜单保存命令不能为空");
        String requiredTenantId = requireText(command.getTenantId(), "租户业务编码不能为空");
        SysMenu menu = new SysMenu();
        menu.setId(command.getMenuId());
        menu.setTenantId(requiredTenantId);
        menu.setParentId(command.getParentId());
        menu.setMenuCode(requireText(command.getMenuCode(), "菜单编码不能为空"));
        menu.setName(requireText(command.getName(), "菜单名称不能为空"));
        menu.setType(normalizeType(command.getType()));
        menu.setPath(trimToNull(command.getPath()));
        menu.setComponent(trimToNull(command.getComponent()));
        menu.setPermission(trimToNull(command.getPermission()));
        menu.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        menu.setStatus(normalizeStatus(command.getStatus()));
        if (command.getMenuId() == null) {
            menuRepository.insert(menu);
            return;
        }
        Long requiredMenuId = requirePositive(command.getMenuId(), "菜单主键不能为空");
        validateTenantMenu(requiredTenantId, requiredMenuId);
        menuRepository.update(menu);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.SYS_MENU_TREE, ZhycCacheNames.SYS_USER_PERMISSIONS},
            allEntries = true)
    public void updateStatus(String tenantId, Long menuId, String status) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredMenuId = requirePositive(menuId, "菜单主键不能为空");
        validateTenantMenu(requiredTenantId, requiredMenuId);
        menuRepository.updateStatus(requiredTenantId, requiredMenuId, normalizeStatus(status));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {ZhycCacheNames.SYS_MENU_TREE, ZhycCacheNames.SYS_USER_PERMISSIONS},
            allEntries = true)
    public void deleteMenu(String tenantId, Long menuId) {
        String requiredTenantId = requireText(tenantId, "租户业务编码不能为空");
        Long requiredMenuId = requirePositive(menuId, "菜单主键不能为空");
        validateTenantMenu(requiredTenantId, requiredMenuId);
        menuRepository.deleteByTenantIdAndId(requiredTenantId, requiredMenuId);
    }

    private void validateTenantMenu(String tenantId, Long menuId) {
        boolean exists = menuRepository.findByTenantId(tenantId).stream()
                .map(SysMenu::getId)
                .filter(Objects::nonNull)
                .anyMatch(menuId::equals);
        if (!exists) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("菜单主键不属于当前租户：" + menuId);
        }
    }

    private String normalizeType(String type) {
        String normalizedType = requireText(type, "菜单类型不能为空").toLowerCase();
        if (!"directory".equals(normalizedType) && !"menu".equals(normalizedType) && !"button".equals(normalizedType)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("菜单类型仅支持 directory、menu、button");
        }
        return normalizedType;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = requireText(status, "菜单状态不能为空").toLowerCase();
        if (!"enabled".equals(normalizedStatus) && !"disabled".equals(normalizedStatus)) {
            throw com.zhyc.system.support.SystemServiceValidation.businessFailure("菜单状态仅支持 enabled 或 disabled");
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
