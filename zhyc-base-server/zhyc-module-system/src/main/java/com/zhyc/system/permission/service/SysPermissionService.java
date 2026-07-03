/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.service;

import java.util.List;

/**
 * 系统权限业务服务。
 */
public interface SysPermissionService {

    /**
     * 查询租户内用户可用权限标识。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 权限标识列表
     */
    List<String> listUserPermissions(String tenantId, Long userId);
}
