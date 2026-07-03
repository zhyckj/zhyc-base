/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.permission.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.permission.service.OpenApiPermissionResponse;
import com.zhyc.openapi.permission.service.OpenApiPermissionSaveCommand;
import com.zhyc.openapi.permission.service.OpenApiPermissionService;
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
 * 开放 API 权限授权管理接口。
 */
@RestController
@RequestMapping("/openapi/api-permissions")
public class OpenApiPermissionController {

    /** 开放 API 授权保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_OPENAPI_PERMISSION_SAVE_REQUEST_REQUIRED";

    /** 开放 API 权限授权业务服务。 */
    private final OpenApiPermissionService permissionService;

    /**
     * 创建开放 API 权限授权管理接口。
     *
     * @param permissionService 开放 API 权限授权业务服务
     */
    public OpenApiPermissionController(OpenApiPermissionService permissionService) {
        this.permissionService = Objects.requireNonNull(permissionService, "开放 API 权限授权业务服务不能为空");
    }

    /**
     * 查询租户指定应用的开放 API 授权列表。
     *
     * @param tenantId 租户业务编码
     * @param appCode 开发者应用编码
     * @return 开放 API 授权列表
     */
    @RequiresPermissions("openapi:api-permission:query")
    @GetMapping
    public ApiResult<List<OpenApiPermissionResponse>> listPermissions(@RequestParam("tenantId") String tenantId,
                                                                      @RequestParam("appCode") String appCode) {
        return ApiResult.ok(permissionService.listPermissions(tenantId, appCode));
    }

    /**
     * 保存或更新开放 API 授权。
     *
     * @param request 开放 API 权限授权保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:api-permission:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiPermissionSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "开放 API 授权保存请求不能为空");
        }
        permissionService.save(new OpenApiPermissionSaveCommand(request.getTenantId(), request.getAppCode(),
                request.getApiCode(), request.getApiName(), request.getHttpMethod(),
                request.getPathPattern(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
