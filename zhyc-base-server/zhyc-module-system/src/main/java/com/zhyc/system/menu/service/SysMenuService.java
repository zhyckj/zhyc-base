/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.service;

import java.util.List;

/**
 * 系统菜单业务服务。
 */
public interface SysMenuService {

    /**
     * 查询租户菜单树。
     *
     * @param tenantId 租户业务编码
     * @return 菜单树节点列表
     */
    List<SysMenuTreeNode> listMenuTree(String tenantId, boolean includeDisabled);

    void saveMenu(SysMenuSaveCommand command);

    void updateStatus(String tenantId, Long menuId, String status);

    void deleteMenu(String tenantId, Long menuId);
}
