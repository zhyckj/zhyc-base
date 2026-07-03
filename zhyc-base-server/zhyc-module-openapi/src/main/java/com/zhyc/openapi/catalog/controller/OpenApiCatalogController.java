/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.openapi.catalog.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.openapi.catalog.service.OpenApiCatalogResponse;
import com.zhyc.openapi.catalog.service.OpenApiCatalogSaveCommand;
import com.zhyc.openapi.catalog.service.OpenApiCatalogService;
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
 * 开放 API 目录管理接口。
 */
@RestController
@RequestMapping("/openapi/catalogs")
public class OpenApiCatalogController {

    /** API 目录保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_OPENAPI_CATALOG_SAVE_REQUEST_REQUIRED";

    /** API 目录业务服务。 */
    private final OpenApiCatalogService catalogService;

    /**
     * 创建开放 API 目录管理接口。
     *
     * @param catalogService API 目录业务服务
     */
    public OpenApiCatalogController(OpenApiCatalogService catalogService) {
        this.catalogService = Objects.requireNonNull(catalogService, "API 目录业务服务不能为空");
    }

    /**
     * 查询指定分组的 API 目录列表。
     *
     * @param groupCode API 分组编码
     * @return API 目录列表
     */
    @RequiresPermissions("openapi:catalog:query")
    @GetMapping
    public ApiResult<List<OpenApiCatalogResponse>> listCatalogs(@RequestParam("groupCode") String groupCode) {
        return ApiResult.ok(catalogService.listCatalogs(groupCode));
    }

    /**
     * 保存或更新 API 目录。
     *
     * @param request API 目录保存请求
     * @return 空响应
     */
    @RequiresPermissions("openapi:catalog:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody OpenApiCatalogSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "API 目录保存请求不能为空");
        }
        catalogService.save(new OpenApiCatalogSaveCommand(request.getApiCode(), request.getApiName(),
                request.getGroupCode(), request.getHttpMethod(), request.getPathPattern(), request.getStatus()));
        return ApiResult.ok(null);
    }
}
