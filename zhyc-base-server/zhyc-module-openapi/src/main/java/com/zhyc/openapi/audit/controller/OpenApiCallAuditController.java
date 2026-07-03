/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.audit.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.openapi.audit.service.OpenApiCallAuditResponse;
import com.zhyc.openapi.audit.service.OpenApiCallAuditService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 开放 API 调用审计管理接口。
 */
@RestController
@RequestMapping("/openapi/call-audits")
public class OpenApiCallAuditController {

    /** 开放 API 调用审计业务服务。 */
    private final OpenApiCallAuditService auditService;

    /**
     * 创建开放 API 调用审计管理接口。
     *
     * @param auditService 开放 API 调用审计业务服务
     */
    public OpenApiCallAuditController(OpenApiCallAuditService auditService) {
        this.auditService = Objects.requireNonNull(auditService, "开放 API 调用审计业务服务不能为空");
    }

    /**
     * 查询租户指定应用的开放 API 调用审计列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 调用审计列表
     */
    @RequiresPermissions("openapi:call-audit:query")
    @GetMapping
    public ApiResult<List<OpenApiCallAuditResponse>> listAudits(@RequestParam("tenantId") String tenantId,
                                                                @RequestParam("appCode") String appCode) {
        return ApiResult.ok(auditService.listAudits(tenantId, appCode));
    }

    /**
     * 查询租户指定应用的开放 API 错误日志列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 错误日志列表
     */
    @RequiresPermissions("openapi:error-log:query")
    @GetMapping("/errors")
    public ApiResult<List<OpenApiCallAuditResponse>> listErrorLogs(@RequestParam("tenantId") String tenantId,
                                                                   @RequestParam("appCode") String appCode) {
        return ApiResult.ok(auditService.listErrorLogs(tenantId, appCode));
    }
}
