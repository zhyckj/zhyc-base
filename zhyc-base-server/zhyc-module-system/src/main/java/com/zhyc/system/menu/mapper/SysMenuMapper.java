/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.menu.mapper;

import com.zhyc.system.menu.domain.SysMenu;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统菜单 MyBatis Mapper。
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 查询租户内启用状态的菜单和权限节点。
     *
     * @param tenantId 租户业务编码
     * @return 菜单列表
     */
    @Results(id = "SysMenuResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "tenantId", property = "tenantId"),
            @Result(column = "parentId", property = "parentId"),
            @Result(column = "menuCode", property = "menuCode"),
            @Result(column = "name", property = "name"),
            @Result(column = "type", property = "type"),
            @Result(column = "path", property = "path"),
            @Result(column = "component", property = "component"),
            @Result(column = "permission", property = "permission"),
            @Result(column = "sortOrder", property = "sortOrder"),
            @Result(column = "status", property = "status"),
            @Result(column = "createdAt", property = "createdAt"),
            @Result(column = "updatedAt", property = "updatedAt")
    })
    @SelectProvider(type = SysMenuSqlProvider.class, method = "selectEnabledByTenantId")
    List<SysMenu> selectEnabledByTenantId(@Param("tenantId") String tenantId);

    /**
     * 查询租户内全部菜单和权限节点。
     *
     * @param tenantId 租户业务编码
     * @return 菜单列表
     */
    @ResultMap("SysMenuResultMap")
    @SelectProvider(type = SysMenuSqlProvider.class, method = "selectByTenantId")
    List<SysMenu> selectByTenantId(@Param("tenantId") String tenantId);

    @InsertProvider(type = SysMenuSqlProvider.class, method = "insert")
    void insert(SysMenu menu);

    @UpdateProvider(type = SysMenuSqlProvider.class, method = "update")
    void update(SysMenu menu);

    @UpdateProvider(type = SysMenuSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("menuId") Long menuId,
                      @Param("status") String status);

    @DeleteProvider(type = SysMenuSqlProvider.class, method = "deleteRoleMenusByTenantAndMenu")
    void deleteRoleMenusByTenantAndMenu(@Param("tenantId") String tenantId, @Param("menuId") Long menuId);

    @DeleteProvider(type = SysMenuSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("menuId") Long menuId);
}
