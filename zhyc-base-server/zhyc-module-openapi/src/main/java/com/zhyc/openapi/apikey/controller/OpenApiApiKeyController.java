/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.apikey.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyResponse;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyRotateCommand;
import com.zhyc.openapi.apikey.service.OpenApiApiKeySaveCommand;
import com.zhyc.openapi.apikey.service.OpenApiApiKeyService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * API Key 管理接口。
 */
@RestController
@RequestMapping("/openapi/api-keys")
public class OpenApiApiKeyController {

    /** API Key 保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_OPENAPI_API_KEY_SAVE_REQUEST_REQUIRED";
    /** API Key Secret 轮换请求缺失错误码。 */
    private static final String ERROR_ROTATE_REQUEST_REQUIRED = "ZHYC_OPENAPI_API_KEY_ROTATE_REQUEST_REQUIRED";

    /** API Key 业务服务。 */
    private final OpenApiApiKeyService apiKeyService;

    /**
     * 创建 API Key 管理接口。
     *
     * @param apiKeyService API Key 业务服务
     */
    public OpenApiApiKeyController(OpenApiApiKeyService apiKeyService) {
        this.apiKeyService = Objects.requireNonNull(apiKeyService, "API Key 业务服务不能为空");
    }

    /**
     * 查询租户指定应用的 API Key 列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return API Key 列表
     */
    @RequiresPermissions("openapi:api-key:query")
    @GetMapping
    public ApiResult<List<OpenApiApiKeyResponse>> listApiKeys(@RequestParam("tenantId") String tenantId,
                                                              @RequestParam("appCode") String appCode) {
        return ApiResult.ok(apiKeyService.listApiKeys(tenantId, appCode));
    }

    /**
     * 保存或更新 API Key。
     *
     * @param request API Key 保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:api-key:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiApiKeySaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "API Key 保存请求不能为空");
        }
        apiKeyService.save(new OpenApiApiKeySaveCommand(request.getTenantId(), request.getAppCode(),
                request.getAccessKey(), request.getSecretCipher(), request.getStatus(), request.getExpireAt()));
        return ApiResult.ok(null);
    }

    /**
     * 轮换 API Key Secret。
     *
     * @param accessKey API 访问密钥
     * @param request API Key Secret 轮换请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:api-key:rotate")
    @PostMapping("/{accessKey}/rotate")
    public ApiResult<Void> rotateSecret(@PathVariable("accessKey") String accessKey,
                                        @RequestBody OpenApiApiKeyRotateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_ROTATE_REQUEST_REQUIRED, "API Key Secret 轮换请求不能为空");
        }
        apiKeyService.rotateSecret(new OpenApiApiKeyRotateCommand(request.getTenantId(), request.getAppCode(),
                accessKey, request.getSecretCipher(), request.getExpireAt()));
        return ApiResult.ok(null);
    }
}
