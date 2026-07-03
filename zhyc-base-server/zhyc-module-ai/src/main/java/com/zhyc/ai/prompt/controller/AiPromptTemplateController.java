/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.prompt.controller;

import com.zhyc.ai.prompt.service.AiPromptTemplateResponse;
import com.zhyc.ai.prompt.service.AiPromptTemplateSaveCommand;
import com.zhyc.ai.prompt.service.AiPromptTemplateService;
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
 * AI 提示词模板管理接口。
 */
@RestController
@RequestMapping("/ai/prompts")
public class AiPromptTemplateController {

    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_AI_PROMPT_SAVE_REQUEST_REQUIRED";

    private final AiPromptTemplateService promptTemplateService;

    public AiPromptTemplateController(AiPromptTemplateService promptTemplateService) {
        this.promptTemplateService = Objects.requireNonNull(promptTemplateService, "AI 提示词模板服务不能为空");
    }

    @RequiresPermissions("ai:prompt:query")
    @GetMapping
    public ApiResult<List<AiPromptTemplateResponse>> listTemplates(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(promptTemplateService.listTemplates(tenantId));
    }

    @RequiresPermissions("ai:prompt:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody AiPromptTemplateSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "AI 提示词模板保存请求不能为空");
        }
        promptTemplateService.save(new AiPromptTemplateSaveCommand(request.getTenantId(), request.getPromptCode(),
                request.getPromptName(), request.getVersion(), request.getTemplateContent(), request.getVariables(),
                request.getStatus()));
        return ApiResult.ok(null);
    }
}
