/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.app.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.app.service.OpenApiAppResponse;
import com.zhyc.openapi.app.service.OpenApiAppSaveCommand;
import com.zhyc.openapi.app.service.OpenApiAppService;
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
 * 开发者应用管理接口。
 */
@RestController
@RequestMapping("/openapi/apps")
public class OpenApiAppController {

    /** 开发者应用保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_OPENAPI_APP_SAVE_REQUEST_REQUIRED";

    /** 开发者应用业务服务。 */
    private final OpenApiAppService appService;

    /**
     * 创建开发者应用管理接口。
     *
     * @param appService 开发者应用业务服务
     */
    public OpenApiAppController(OpenApiAppService appService) {
        this.appService = Objects.requireNonNull(appService, "开发者应用业务服务不能为空");
    }

    /**
     * 查询租户开发者应用列表。
     *
     * @param tenantId 租户业务编码
     * @return 开发者应用列表
     */
    @RequiresPermissions("openapi:app:query")
    @GetMapping
    public ApiResult<List<OpenApiAppResponse>> listApps(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(appService.listApps(tenantId));
    }

    /**
     * 保存或更新开发者应用。
     *
     * @param request 开发者应用保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:app:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiAppSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "开发者应用保存请求不能为空");
        }
        appService.save(new OpenApiAppSaveCommand(request.getTenantId(), request.getAppCode(),
                request.getAppName(), request.getOwnerUserId(), request.getAuthMode(),
                request.getIpWhitelist(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
