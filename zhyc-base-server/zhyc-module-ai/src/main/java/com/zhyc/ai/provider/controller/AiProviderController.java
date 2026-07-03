/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.provider.controller;

import com.zhyc.ai.provider.service.AiProviderResponse;
import com.zhyc.ai.provider.service.AiProviderSaveCommand;
import com.zhyc.ai.provider.service.AiProviderService;
import com.zhyc.ai.provider.service.AiProviderTestCommand;
import com.zhyc.ai.provider.service.AiProviderTestResponse;
import com.zhyc.ai.provider.service.AiProviderTestService;
import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * AI 模型供应商管理接口。
 */
@RestController
@RequestMapping("/ai/providers")
public class AiProviderController {

    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_AI_PROVIDER_SAVE_REQUEST_REQUIRED";
    private static final String ERROR_TEST_REQUEST_REQUIRED = "ZHYC_AI_PROVIDER_TEST_REQUEST_REQUIRED";

    private final AiProviderService providerService;
    private final AiProviderTestService providerTestService;

    public AiProviderController(AiProviderService providerService, AiProviderTestService providerTestService) {
        this.providerService = Objects.requireNonNull(providerService, "AI 模型供应商服务不能为空");
        this.providerTestService = Objects.requireNonNull(providerTestService, "AI 模型供应商测试服务不能为空");
    }

    @RequiresPermissions("ai:provider:query")
    @GetMapping
    public ApiResult<List<AiProviderResponse>> listProviders(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(providerService.listProviders(tenantId));
    }

    @RequiresPermissions("ai:provider:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody AiProviderSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "AI 模型供应商保存请求不能为空");
        }
        providerService.save(new AiProviderSaveCommand(request.getTenantId(), request.getProviderCode(),
                request.getProviderName(), request.getProviderType(), request.getBaseUrl(), request.getSecretRef(),
                request.getStatus()));
        return ApiResult.ok(null);
    }

    @RequiresPermissions("ai:provider:test")
    @PostMapping("/test")
    public ApiResult<AiProviderTestResponse> testProvider(@RequestBody AiProviderSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_TEST_REQUEST_REQUIRED, "AI 模型供应商测试请求不能为空");
        }
        return ApiResult.ok(providerTestService.test(new AiProviderTestCommand(request.getTenantId(),
                request.getProviderCode(), request.getProviderType(), request.getBaseUrl(), request.getSecretRef())));
    }
}
