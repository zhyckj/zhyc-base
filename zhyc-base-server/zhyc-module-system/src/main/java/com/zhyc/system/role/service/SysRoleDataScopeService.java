/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.service;

import java.util.List;

/**
 * 系统角色自定义数据权限业务服务。
 */
public interface SysRoleDataScopeService {

    /**
     * 查询租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 角色自定义数据权限响应列表
     */
    List<SysRoleDataScopeResponse> listRoleDataScopes(String tenantId, Long roleId);

    /**
     * 替换租户内角色自定义组织范围。
     *
     * @param command 角色自定义数据权限绑定命令
     */
    void bindRoleDataScopes(RoleDataScopeBindCommand command);
}
