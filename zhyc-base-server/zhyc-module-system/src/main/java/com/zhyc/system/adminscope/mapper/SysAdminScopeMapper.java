/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.adminscope.mapper;

import com.zhyc.system.adminscope.domain.SysAdminScope;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统管理员管理范围 MyBatis Mapper。
 */
@Mapper
public interface SysAdminScopeMapper {

    /**
     * 查询租户内管理员管理范围。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @return 管理员管理范围列表
     */
    @SelectProvider(type = SysAdminScopeSqlProvider.class, method = "selectByTenantIdAndUserId")
    List<SysAdminScope> selectByTenantIdAndUserId(@Param("tenantId") String tenantId,
                                                  @Param("userId") Long userId);

    /**
     * 删除租户内管理员管理范围。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     */
    @DeleteProvider(type = SysAdminScopeSqlProvider.class, method = "deleteByTenantIdAndUserId")
    void deleteByTenantIdAndUserId(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    /**
     * 新增租户内管理员管理范围。
     *
     * @param tenantId 租户业务编码
     * @param userId 管理员用户主键
     * @param scopeType 范围类型
     * @param scopeRefCode 范围引用编码
     */
    @InsertProvider(type = SysAdminScopeSqlProvider.class, method = "insertAdminScope")
    void insertAdminScope(@Param("tenantId") String tenantId, @Param("userId") Long userId,
                          @Param("scopeType") String scopeType, @Param("scopeRefCode") String scopeRefCode);
}
