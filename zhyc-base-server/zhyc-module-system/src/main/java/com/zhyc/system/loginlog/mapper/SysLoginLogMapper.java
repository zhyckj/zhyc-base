/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.mapper;

import com.zhyc.system.loginlog.domain.SysLoginLog;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统登录日志 MyBatis Mapper。
 */
@Mapper
public interface SysLoginLogMapper {

    /**
     * 新增系统登录日志。
     *
     * @param loginLog 系统登录日志
     */
    @InsertProvider(type = SysLoginLogSqlProvider.class, method = "insert")
    void insert(SysLoginLog loginLog);

    /**
     * 查询租户最近登录日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    @SelectProvider(type = SysLoginLogSqlProvider.class, method = "selectRecentByTenantId")
    List<SysLoginLog> selectRecentByTenantId(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
