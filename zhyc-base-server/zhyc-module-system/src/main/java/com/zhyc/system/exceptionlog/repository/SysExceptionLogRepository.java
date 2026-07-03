/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.exceptionlog.repository;

import com.zhyc.system.exceptionlog.domain.SysExceptionLog;

import java.util.List;

/**
 * 系统异常日志仓储接口。
 */
public interface SysExceptionLogRepository {

    /**
     * 保存系统异常日志。
     *
     * @param exceptionLog 系统异常日志
     */
    void save(SysExceptionLog exceptionLog);

    /**
     * 查询租户最近异常日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近异常日志列表
     */
    List<SysExceptionLog> findRecentByTenantId(String tenantId, int limit);
}
