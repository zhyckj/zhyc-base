/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.signature.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicyResponse;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicySaveCommand;
import com.zhyc.openapi.signature.service.OpenApiSignaturePolicyService;
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
 * 开放 API 签名策略管理接口。
 */
@RestController
@RequestMapping("/openapi/signature-policies")
public class OpenApiSignaturePolicyController {

    /** 签名策略保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED =
            "ZHYC_OPENAPI_SIGNATURE_POLICY_SAVE_REQUEST_REQUIRED";

    /** 签名策略业务服务。 */
    private final OpenApiSignaturePolicyService signaturePolicyService;

    /**
     * 创建开放 API 签名策略管理接口。
     *
     * @param signaturePolicyService 签名策略业务服务
     */
    public OpenApiSignaturePolicyController(OpenApiSignaturePolicyService signaturePolicyService) {
        this.signaturePolicyService = Objects.requireNonNull(signaturePolicyService, "签名策略业务服务不能为空");
    }

    /**
     * 查询租户指定应用的签名策略列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 签名策略列表
     */
    @RequiresPermissions("openapi:signature-policy:query")
    @GetMapping
    public ApiResult<List<OpenApiSignaturePolicyResponse>> listPolicies(@RequestParam("tenantId") String tenantId,
                                                                        @RequestParam("appCode") String appCode) {
        return ApiResult.ok(signaturePolicyService.listPolicies(tenantId, appCode));
    }

    /**
     * 保存或更新签名策略。
     *
     * @param request 签名策略保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:signature-policy:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiSignaturePolicySaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "签名策略保存请求不能为空");
        }
        signaturePolicyService.save(new OpenApiSignaturePolicySaveCommand(request.getTenantId(),
                request.getAppCode(), request.getAlgorithm(), request.getTimestampToleranceSeconds(),
                request.getNonceTtlSeconds(), request.getRequireBodyHash(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
