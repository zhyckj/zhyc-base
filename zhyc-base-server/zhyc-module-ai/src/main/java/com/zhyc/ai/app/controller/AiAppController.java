/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.app.controller;

import com.zhyc.ai.app.service.AiAppResponse;
import com.zhyc.ai.app.service.AiAppSaveCommand;
import com.zhyc.ai.app.service.AiAppService;
import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * AI 应用接入管理接口。
 */
@RestController
@RequestMapping("/ai/apps")
public class AiAppController {

    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_AI_APP_SAVE_REQUEST_REQUIRED";

    private final AiAppService appService;

    public AiAppController(AiAppService appService) {
        this.appService = Objects.requireNonNull(appService, "AI 应用接入服务不能为空");
    }

    @RequiresPermissions("ai:app:query")
    @GetMapping
    public ApiResult<List<AiAppResponse>> listApps(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(appService.listApps(tenantId));
    }

    @RequiresPermissions("ai:app:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody AiAppSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "AI 应用接入保存请求不能为空");
        }
        appService.save(new AiAppSaveCommand(request.getTenantId(), request.getAppCode(), request.getAppName(),
                request.getDefaultModelId(), request.getSystemPrompt(), request.getDailyTokenQuota(),
                request.getStatus()));
        return ApiResult.ok(null);
    }
}
