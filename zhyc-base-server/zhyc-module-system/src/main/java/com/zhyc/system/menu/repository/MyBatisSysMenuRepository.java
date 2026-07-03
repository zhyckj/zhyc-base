/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.repository;

import com.zhyc.system.menu.domain.SysMenu;
import com.zhyc.system.menu.mapper.SysMenuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * 基于 MyBatis 的系统菜单仓储实现。
 */
@Repository
public class MyBatisSysMenuRepository implements SysMenuRepository {

    /** 系统菜单 Mapper。 */
    private final SysMenuMapper menuMapper;

    /**
     * 创建系统菜单仓储实现。
     *
     * @param menuMapper 系统菜单 Mapper
     */
    public MyBatisSysMenuRepository(SysMenuMapper menuMapper) {
        this.menuMapper = Objects.requireNonNull(menuMapper, "系统菜单 Mapper 不能为空");
    }

    @Override
    public List<SysMenu> findEnabledByTenantId(String tenantId) {
        return menuMapper.selectEnabledByTenantId(tenantId);
    }

    @Override
    public List<SysMenu> findByTenantId(String tenantId) {
        return menuMapper.selectByTenantId(tenantId);
    }

    @Override
    public void insert(SysMenu menu) {
        menuMapper.insert(menu);
    }

    @Override
    public void update(SysMenu menu) {
        menuMapper.update(menu);
    }

    @Override
    public void updateStatus(String tenantId, Long menuId, String status) {
        menuMapper.updateStatus(tenantId, menuId, status);
    }

    @Override
    public void deleteByTenantIdAndId(String tenantId, Long menuId) {
        menuMapper.deleteRoleMenusByTenantAndMenu(tenantId, menuId);
        menuMapper.deleteByTenantIdAndId(tenantId, menuId);
    }
}
