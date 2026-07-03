/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.mapper;

import com.zhyc.system.exceptionlog.domain.SysExceptionLog;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 系统异常日志 MyBatis Mapper。
 */
@Mapper
public interface SysExceptionLogMapper {

    /**
     * 新增系统异常日志。
     *
     * @param exceptionLog 系统异常日志
     */
    @InsertProvider(type = SysExceptionLogSqlProvider.class, method = "insert")
    void insert(SysExceptionLog exceptionLog);

    /**
     * 查询租户最近异常日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近异常日志列表
     */
    @SelectProvider(type = SysExceptionLogSqlProvider.class, method = "selectRecentByTenantId")
    List<SysExceptionLog> selectRecentByTenantId(@Param("tenantId") String tenantId, @Param("limit") int limit);
}
