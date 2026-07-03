/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.oauthclient.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientResponse;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientSaveCommand;
import com.zhyc.openapi.oauthclient.service.OpenApiOauthClientService;
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
 * 开放平台 OAuth2 客户端映射管理接口。
 */
@RestController
@RequestMapping("/openapi/oauth-clients")
public class OpenApiOauthClientController {

    /** OAuth2 客户端映射保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_OPENAPI_OAUTH_CLIENT_SAVE_REQUEST_REQUIRED";

    /** OAuth2 客户端映射业务服务。 */
    private final OpenApiOauthClientService oauthClientService;

    /**
     * 创建 OAuth2 客户端映射管理接口。
     *
     * @param oauthClientService OAuth2 客户端映射业务服务
     */
    public OpenApiOauthClientController(OpenApiOauthClientService oauthClientService) {
        this.oauthClientService = Objects.requireNonNull(oauthClientService, "OAuth2 客户端映射业务服务不能为空");
    }

    /**
     * 查询租户指定应用的 OAuth2 客户端映射列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return OAuth2 客户端映射列表
     */
    @RequiresPermissions("openapi:oauth-client:query")
    @GetMapping
    public ApiResult<List<OpenApiOauthClientResponse>> listClients(@RequestParam("tenantId") String tenantId,
                                                                   @RequestParam("appCode") String appCode) {
        return ApiResult.ok(oauthClientService.listClients(tenantId, appCode));
    }

    /**
     * 保存或更新 OAuth2 客户端映射。
     *
     * @param request OAuth2 客户端映射保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:oauth-client:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiOauthClientSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "OAuth2 客户端映射保存请求不能为空");
        }
        oauthClientService.save(new OpenApiOauthClientSaveCommand(request.getTenantId(), request.getAppCode(),
                request.getClientId(), request.getAllowedScopes(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
