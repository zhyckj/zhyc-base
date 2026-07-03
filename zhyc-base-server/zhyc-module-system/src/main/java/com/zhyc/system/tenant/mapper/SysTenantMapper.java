/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.mapper;

import com.zhyc.system.tenant.domain.Tenant;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统租户 MyBatis Mapper。
 */
@Mapper
public interface SysTenantMapper {

    /**
     * 按租户状态查询租户列表。
     *
     * @param status 租户状态
     * @return 租户列表
     */
    @SelectProvider(type = SysTenantSqlProvider.class, method = "selectByStatus")
    List<Tenant> selectByStatus(@Param("status") String status);

    /**
     * 按登录账号查询可访问的启用租户列表。
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    @SelectProvider(type = SysTenantSqlProvider.class, method = "selectAuthorizedByUsername")
    List<Tenant> selectAuthorizedByUsername(@Param("username") String username);

    /**
     * 按租户业务编码查询租户主记录。
     *
     * @param tenantId 租户业务编码
     * @return 租户主记录
     */
    @SelectProvider(type = SysTenantSqlProvider.class, method = "selectByTenantId")
    Tenant selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 新增或更新租户基础信息。
     *
     * @param tenant 租户基础信息
     */
    @InsertProvider(type = SysTenantSqlProvider.class, method = "upsertTenant")
    void upsertTenant(Tenant tenant);

    /**
     * 更新租户基础信息。
     *
     * @param tenant 租户基础信息
     */
    @UpdateProvider(type = SysTenantSqlProvider.class, method = "updateTenant")
    void updateTenant(Tenant tenant);

    /**
     * 修改租户状态。
     *
     * @param tenantId 租户业务编码
     * @param status 目标状态
     */
    @UpdateProvider(type = SysTenantSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    /**
     * 删除系统租户主记录。
     *
     * @param tenantId 租户业务编码
     */
    @DeleteProvider(type = SysTenantSqlProvider.class, method = "deleteByTenantId")
    void deleteByTenantId(@Param("tenantId") String tenantId);
}
