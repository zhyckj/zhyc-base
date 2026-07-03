/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.version.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.version.service.OpenApiVersionPublishCommand;
import com.zhyc.openapi.version.service.OpenApiVersionResponse;
import com.zhyc.openapi.version.service.OpenApiVersionService;
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
 * 开放 API 版本发布管理接口。
 */
@RestController
@RequestMapping("/openapi/versions")
public class OpenApiVersionController {

    /** API 版本发布请求缺失错误码。 */
    private static final String ERROR_PUBLISH_REQUEST_REQUIRED = "ZHYC_OPENAPI_VERSION_PUBLISH_REQUEST_REQUIRED";

    /** API 版本发布业务服务。 */
    private final OpenApiVersionService versionService;

    /**
     * 创建开放 API 版本发布管理接口。
     *
     * @param versionService API 版本发布业务服务
     */
    public OpenApiVersionController(OpenApiVersionService versionService) {
        this.versionService = Objects.requireNonNull(versionService, "API 版本发布业务服务不能为空");
    }

    /**
     * 查询指定 API 的版本列表。
     *
     * @param apiCode API 业务编码
     * @return API 版本列表
     */
    @RequiresPermissions("openapi:catalog:query")
    @GetMapping
    public ApiResult<List<OpenApiVersionResponse>> listVersions(@RequestParam("apiCode") String apiCode) {
        return ApiResult.ok(versionService.listVersions(apiCode));
    }

    /**
     * 发布或更新 API 版本。
     *
     * @param request API 版本发布请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:catalog:publish")
    @PutMapping
    public ApiResult<Void> publish(@RequestBody OpenApiVersionPublishRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_PUBLISH_REQUEST_REQUIRED, "API 版本发布请求不能为空");
        }
        versionService.publish(new OpenApiVersionPublishCommand(request.getApiCode(), request.getVersion(),
                request.getBackendRoute(), request.getRequestSchema(), request.getResponseSchema(),
                request.getStatus()));
        return ApiResult.ok(null);
    }
}
