/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.repository;

import com.zhyc.system.menu.domain.SysMenu;

import java.util.List;

/**
 * 系统菜单仓储接口。
 */
public interface SysMenuRepository {

    /**
     * 查询租户内启用状态的菜单和权限节点。
     *
     * @param tenantId 租户业务编码
     * @return 租户内启用菜单列表
     */
    List<SysMenu> findEnabledByTenantId(String tenantId);

    List<SysMenu> findByTenantId(String tenantId);

    void insert(SysMenu menu);

    void update(SysMenu menu);

    void updateStatus(String tenantId, Long menuId, String status);

    void deleteByTenantIdAndId(String tenantId, Long menuId);
}
