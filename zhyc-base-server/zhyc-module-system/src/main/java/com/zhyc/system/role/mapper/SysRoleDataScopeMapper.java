/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.mapper;

import com.zhyc.system.role.domain.SysRoleDataScope;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统角色自定义数据权限 MyBatis Mapper。
 */
@Mapper
public interface SysRoleDataScopeMapper {

    /**
     * 查询租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 角色自定义数据权限列表
     */
    @SelectProvider(type = SysRoleDataScopeSqlProvider.class, method = "selectByTenantIdAndRoleId")
    List<SysRoleDataScope> selectByTenantIdAndRoleId(@Param("tenantId") String tenantId,
                                                     @Param("roleId") Long roleId);

    /**
     * 删除租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    @DeleteProvider(type = SysRoleDataScopeSqlProvider.class, method = "deleteByTenantIdAndRoleId")
    void deleteByTenantIdAndRoleId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 新增租户内角色自定义组织范围。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param orgId 授权组织主键
     * @param scopeType 范围类型
     */
    @InsertProvider(type = SysRoleDataScopeSqlProvider.class, method = "insertRoleDataScope")
    void insertRoleDataScope(@Param("tenantId") String tenantId, @Param("roleId") Long roleId,
                             @Param("orgId") Long orgId, @Param("scopeType") String scopeType);
}
