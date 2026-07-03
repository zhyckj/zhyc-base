/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.loginlog.service;

import java.util.List;

/**
 * 系统登录日志业务服务。
 */
public interface SysLoginLogService {

    /**
     * 记录系统登录日志。
     *
     * @param command 登录日志记录命令
     */
    void record(SysLoginLogRecordCommand command);

    /**
     * 查询租户最近登录日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近登录日志列表
     */
    List<SysLoginLogResponse> listRecent(String tenantId, int limit);
}
