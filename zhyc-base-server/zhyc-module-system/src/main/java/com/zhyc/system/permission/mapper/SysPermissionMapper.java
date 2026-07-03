/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统权限 MyBatis Mapper。
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 查询租户内用户已授权的权限标识。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 权限标识列表
     */
    @SelectProvider(type = SysPermissionSqlProvider.class, method = "selectPermissionsByTenantAndUser")
    List<String> selectPermissionsByTenantAndUser(@Param("tenantId") String tenantId, @Param("userId") Long userId);
}
