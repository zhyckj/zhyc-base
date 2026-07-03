/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.audit.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.system.audit.service.SysAuditLogResponse;
import com.zhyc.system.audit.service.SysAuditLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 系统审计日志接口。
 */
@RestController
@RequestMapping("/system/audit-logs")
public class SysAuditLogController {

    /** 系统审计日志业务服务。 */
    private final SysAuditLogService auditLogService;

    /**
     * 创建系统审计日志接口。
     *
     * @param auditLogService 系统审计日志业务服务
     */
    public SysAuditLogController(SysAuditLogService auditLogService) {
        this.auditLogService = Objects.requireNonNull(auditLogService, "系统审计日志业务服务不能为空");
    }

    /**
     * 查询租户最近审计日志。
     *
     * @param tenantId 租户业务编码
     * @param limit 查询条数上限
     * @return 最近审计日志列表
     */
    @RequiresPermissions("system:audit:query")
    @GetMapping("/recent")
    public ApiResult<List<SysAuditLogResponse>> listRecent(@RequestParam("tenantId") String tenantId,
                                                           @RequestParam(value = "limit", defaultValue = "50")
                                                           int limit) {
        return ApiResult.ok(auditLogService.listRecent(tenantId, limit));
    }
}
