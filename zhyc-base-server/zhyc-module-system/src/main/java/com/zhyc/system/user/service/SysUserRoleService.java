/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.service;

import java.util.List;

/**
 * 系统用户角色业务服务。
 */
public interface SysUserRoleService {

    /**
     * 查询租户内用户角色列表。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 用户角色响应列表
     */
    List<SysUserRoleResponse> listUserRoles(String tenantId, Long userId);

    /**
     * 替换租户内用户角色绑定。
     *
     * @param command 用户角色绑定命令
     */
    void bindUserRoles(SysUserRoleBindCommand command);
}
