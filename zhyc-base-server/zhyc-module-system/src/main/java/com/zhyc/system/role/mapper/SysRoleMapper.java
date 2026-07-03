/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.role.mapper;

import com.zhyc.system.role.domain.SysRole;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统角色 MyBatis Mapper。
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 查询租户内角色列表。
     *
     * @param tenantId 租户业务编码
     * @return 角色列表
     */
    @SelectProvider(type = SysRoleSqlProvider.class, method = "selectByTenantId")
    List<SysRole> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 新增系统角色。
     *
     * @param role 系统角色
     */
    @InsertProvider(type = SysRoleSqlProvider.class, method = "insert")
    void insert(SysRole role);

    /**
     * 更新系统角色。
     *
     * @param role 系统角色
     */
    @UpdateProvider(type = SysRoleSqlProvider.class, method = "update")
    void update(SysRole role);

    /**
     * 更新系统角色状态。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param status 角色状态
     */
    @UpdateProvider(type = SysRoleSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("roleId") Long roleId, @Param("status") String status);

    /**
     * 删除租户内指定角色的已有菜单绑定。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    @DeleteProvider(type = SysRoleSqlProvider.class, method = "deleteRoleMenusByTenantAndRole")
    void deleteRoleMenusByTenantAndRole(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 删除租户内指定角色的数据权限绑定。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    @DeleteProvider(type = SysRoleSqlProvider.class, method = "deleteRoleDataScopesByTenantAndRole")
    void deleteRoleDataScopesByTenantAndRole(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 删除租户内指定角色的用户绑定。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    @DeleteProvider(type = SysRoleSqlProvider.class, method = "deleteUserRolesByTenantAndRole")
    void deleteUserRolesByTenantAndRole(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 删除租户内指定角色。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     */
    @DeleteProvider(type = SysRoleSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    /**
     * 查询租户内指定角色的菜单绑定。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @return 菜单主键列表
     */
    @SelectProvider(type = SysRoleSqlProvider.class, method = "selectRoleMenuIdsByTenantAndRole")
    List<Long> selectRoleMenuIdsByTenantAndRole(@Param("tenantId") String tenantId,
                                                @Param("roleId") Long roleId);

    /**
     * 新增租户内角色菜单绑定。
     *
     * @param tenantId 租户业务编码
     * @param roleId 角色主键
     * @param menuId 菜单主键
     */
    @InsertProvider(type = SysRoleSqlProvider.class, method = "insertRoleMenu")
    void insertRoleMenu(@Param("tenantId") String tenantId, @Param("roleId") Long roleId,
                        @Param("menuId") Long menuId);
}
