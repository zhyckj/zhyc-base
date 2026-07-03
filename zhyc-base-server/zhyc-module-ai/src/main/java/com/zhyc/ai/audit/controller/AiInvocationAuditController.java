/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.audit.controller;

import com.zhyc.ai.audit.service.AiInvocationAuditRecordCommand;
import com.zhyc.ai.audit.service.AiInvocationAuditResponse;
import com.zhyc.ai.audit.service.AiInvocationAuditService;
import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * AI 调用审计管理接口。
 */
@RestController
@RequestMapping("/ai/invocation-audits")
public class AiInvocationAuditController {

    private static final String ERROR_RECORD_REQUEST_REQUIRED = "ZHYC_AI_AUDIT_RECORD_REQUEST_REQUIRED";

    private final AiInvocationAuditService auditService;

    public AiInvocationAuditController(AiInvocationAuditService auditService) {
        this.auditService = Objects.requireNonNull(auditService, "AI 调用审计服务不能为空");
    }

    @RequiresPermissions("ai:audit:query")
    @GetMapping
    public ApiResult<List<AiInvocationAuditResponse>> listAudits(@RequestParam("tenantId") String tenantId,
                                                                 @RequestParam("appCode") String appCode) {
        return ApiResult.ok(auditService.listAudits(tenantId, appCode));
    }

    @RequiresPermissions("ai:audit:record")
    @PostMapping
    public ApiResult<Void> record(@RequestBody AiInvocationAuditRecordRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_RECORD_REQUEST_REQUIRED, "AI 调用审计记录请求不能为空");
        }
        auditService.record(new AiInvocationAuditRecordCommand(request.getTenantId(), request.getAppCode(),
                request.getProviderId(), request.getModelId(), request.getInvocationType(), request.getPromptTokens(),
                request.getCompletionTokens(), request.getTotalTokens(), request.getLatencyMs(), request.getStatus(),
                request.getErrorMessage(), request.getTraceId()));
        return ApiResult.ok(null);
    }
}
