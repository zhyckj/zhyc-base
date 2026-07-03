/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.user.mapper;

import com.zhyc.system.user.domain.SysUserRole;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统用户角色 MyBatis Mapper。
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * 查询租户内指定用户的角色绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @return 用户角色绑定列表
     */
    @SelectProvider(type = SysUserRoleSqlProvider.class, method = "selectByTenantIdAndUserId")
    List<SysUserRole> selectByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 删除租户内指定用户的角色绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     */
    @DeleteProvider(type = SysUserRoleSqlProvider.class, method = "deleteByTenantIdAndUserId")
    void deleteByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 新增租户内用户角色绑定。
     *
     * @param tenantId 租户业务编码
     * @param userId 用户主键
     * @param roleId 角色主键
     */
    @InsertProvider(type = SysUserRoleSqlProvider.class, method = "insertUserRole")
    void insertUserRole(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                        @Param("roleId") Long roleId);
}
