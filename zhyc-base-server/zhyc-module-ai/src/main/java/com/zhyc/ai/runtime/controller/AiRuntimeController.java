/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.ai.runtime.controller;

import com.zhyc.ai.runtime.service.AiRuntimeChatCommand;
import com.zhyc.ai.runtime.service.AiRuntimeChatResponse;
import com.zhyc.ai.runtime.service.AiRuntimeService;
import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * AI 运行时统一调用接口。
 */
@RestController
@RequestMapping("/ai/runtime")
public class AiRuntimeController {

    private static final String ERROR_CHAT_REQUEST_REQUIRED = "ZHYC_AI_RUNTIME_CHAT_REQUEST_REQUIRED";

    private final AiRuntimeService runtimeService;

    public AiRuntimeController(AiRuntimeService runtimeService) {
        this.runtimeService = Objects.requireNonNull(runtimeService, "AI 运行时服务不能为空");
    }

    @RequiresPermissions("ai:runtime:chat")
    @PostMapping("/chat")
    public ApiResult<AiRuntimeChatResponse> chat(@RequestBody AiRuntimeChatRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_CHAT_REQUEST_REQUIRED, "AI 运行时对话请求不能为空");
        }
        return ApiResult.ok(runtimeService.chat(new AiRuntimeChatCommand(request.getTenantId(), request.getAppCode(),
                request.getPromptCode(), request.getPromptVersion(), request.getVariables(), request.isStream())));
    }
}
