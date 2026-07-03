/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.service;

import com.zhyc.common.audit.AuditEvent;

import java.util.List;

/**
 * 系统审计日志业务服务。
 */
public interface SysAuditLogService {

    /**
     * 记录系统审计事件。
     *
     * @param event 公共审计事件
     */
    void record(AuditEvent event);

    /**
     * 查询租户最近审计日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近审计日志列表
     */
    List<SysAuditLogResponse> listRecent(String tenantId, int limit);
}
