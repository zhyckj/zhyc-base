/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.model.controller;

import com.zhyc.ai.model.service.AiModelConfigResponse;
import com.zhyc.ai.model.service.AiModelConfigSaveCommand;
import com.zhyc.ai.model.service.AiModelConfigService;
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
 * AI 模型配置管理接口。
 */
@RestController
@RequestMapping("/ai/models")
public class AiModelConfigController {

    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_AI_MODEL_SAVE_REQUEST_REQUIRED";

    private final AiModelConfigService modelConfigService;

    public AiModelConfigController(AiModelConfigService modelConfigService) {
        this.modelConfigService = Objects.requireNonNull(modelConfigService, "AI 模型配置服务不能为空");
    }

    @RequiresPermissions("ai:model:query")
    @GetMapping
    public ApiResult<List<AiModelConfigResponse>> listModels(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(modelConfigService.listModels(tenantId));
    }

    @RequiresPermissions("ai:model:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody AiModelConfigSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "AI 模型配置保存请求不能为空");
        }
        modelConfigService.save(new AiModelConfigSaveCommand(request.getTenantId(), request.getProviderId(),
                request.getModelCode(), request.getModelName(), request.getModelType(), request.getContextWindow(),
                request.isSupportStream(), request.isSupportTool(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
