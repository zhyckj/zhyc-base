/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.repository;

import com.zhyc.system.loginlog.domain.SysLoginLog;

import java.util.List;

/**
 * 系统登录日志仓储接口。
 */
public interface SysLoginLogRepository {

    /**
     * 保存系统登录日志。
     *
     * @param loginLog 系统登录日志
     */
    void save(SysLoginLog loginLog);

    /**
     * 查询租户最近登录日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    List<SysLoginLog> findRecentByTenantId(String tenantId, int limit);
}
