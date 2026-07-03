/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.org.mapper;

import com.zhyc.system.org.domain.SysOrg;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 系统组织机构 MyBatis Mapper。
 */
@Mapper
public interface SysOrgMapper {

    /**
     * 查询租户内启用状态的组织机构。
     *
     * @param tenantId 租户业务编码
     * @return 组织机构列表
     */
    @SelectProvider(type = SysOrgSqlProvider.class, method = "selectByTenantId")
    List<SysOrg> selectByTenantId(@Param("tenantId") String tenantId);

    @InsertProvider(type = SysOrgSqlProvider.class, method = "insert")
    void insert(SysOrg org);

    @UpdateProvider(type = SysOrgSqlProvider.class, method = "update")
    void update(SysOrg org);

    @UpdateProvider(type = SysOrgSqlProvider.class, method = "updateStatus")
    void updateStatus(@Param("tenantId") String tenantId, @Param("orgId") Long orgId, @Param("status") String status);

    @DeleteProvider(type = SysOrgSqlProvider.class, method = "deleteByTenantIdAndId")
    void deleteByTenantIdAndId(@Param("tenantId") String tenantId, @Param("orgId") Long orgId);
}
