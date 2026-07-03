/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.ratelimit.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicyResponse;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicySaveCommand;
import com.zhyc.openapi.ratelimit.service.OpenApiRateLimitPolicyService;
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
 * 开放 API 限流策略管理接口。
 */
@RestController
@RequestMapping("/openapi/rate-limit-policies")
public class OpenApiRateLimitPolicyController {

    /** 限流策略保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED =
            "ZHYC_OPENAPI_RATE_LIMIT_POLICY_SAVE_REQUEST_REQUIRED";

    /** 限流策略业务服务。 */
    private final OpenApiRateLimitPolicyService rateLimitPolicyService;

    /**
     * 创建开放 API 限流策略管理接口。
     *
     * @param rateLimitPolicyService 限流策略业务服务
     */
    public OpenApiRateLimitPolicyController(OpenApiRateLimitPolicyService rateLimitPolicyService) {
        this.rateLimitPolicyService = Objects.requireNonNull(rateLimitPolicyService, "限流策略业务服务不能为空");
    }

    /**
     * 查询租户指定应用的限流策略列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 限流策略列表
     */
    @RequiresPermissions("openapi:rate-limit-policy:query")
    @GetMapping
    public ApiResult<List<OpenApiRateLimitPolicyResponse>> listPolicies(@RequestParam("tenantId") String tenantId,
                                                                        @RequestParam("appCode") String appCode) {
        return ApiResult.ok(rateLimitPolicyService.listPolicies(tenantId, appCode));
    }

    /**
     * 保存或更新限流策略。
     *
     * @param request 限流策略保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:rate-limit-policy:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiRateLimitPolicySaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "限流策略保存请求不能为空");
        }
        rateLimitPolicyService.save(new OpenApiRateLimitPolicySaveCommand(request.getTenantId(), request.getAppCode(),
                request.getApiCode(), request.getLimitCount(), request.getWindowSeconds(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
