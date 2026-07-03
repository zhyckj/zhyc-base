/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackage.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackage.service.SysTenantPackageResponse;
import com.zhyc.system.tenantpackage.service.SysTenantPackageService;
import com.zhyc.system.tenantpackage.service.TenantPackageCreateCommand;
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
 * 系统租户套餐管理接口。
 */
@RestController
@RequestMapping("/system/tenant-packages")
public class SysTenantPackageController {

    /** 租户套餐状态变更请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_TENANT_PACKAGE_STATUS_REQUEST_REQUIRED";
    /** 租户套餐创建请求缺失错误码。 */
    private static final String ERROR_CREATE_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_TENANT_PACKAGE_CREATE_REQUEST_REQUIRED";

    /** 系统租户套餐业务服务。 */
    private final SysTenantPackageService tenantPackageService;

    /**
     * 创建系统租户套餐管理接口。
     *
     * @param tenantPackageService 系统租户套餐业务服务
     */
    public SysTenantPackageController(SysTenantPackageService tenantPackageService) {
        this.tenantPackageService = Objects.requireNonNull(tenantPackageService, "系统租户套餐业务服务不能为空");
    }

    /**
     * 按状态查询系统租户套餐列表。
     *
     * @param status 套餐状态
     * @return 系统租户套餐列表
     */
    @RequiresPermissions("system:tenant-package:query")
    @GetMapping
    public ApiResult<List<SysTenantPackageResponse>> listPackages(@RequestParam("status") String status) {
        return ApiResult.ok(tenantPackageService.listPackages(status));
    }

    /**
     * 创建系统租户套餐。
     *
     * @param request 租户套餐创建请求
     * @return 创建后的系统租户套餐
     */
    @RequiresPermissions("system:tenant-package:update")
    @PostMapping
    public ApiResult<SysTenantPackageResponse> createPackage(@RequestBody SysTenantPackageCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "套餐创建请求不能为空");
        }
        TenantPackageCreateCommand command = new TenantPackageCreateCommand(request.getPackageCode(),
                request.getPackageName(), request.getStatus(), request.getMaxUserCount(), request.getMaxStorageMb());
        return ApiResult.ok(tenantPackageService.createPackage(command));
    }

    /**
     * 修改系统租户套餐状态。
     *
     * @param packageCode 套餐编码
     * @param request 套餐状态变更请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant-package:update")
    @PutMapping("/{packageCode}/status")
    public ApiResult<Void> changeStatus(@PathVariable("packageCode") String packageCode,
                                        @RequestBody SysTenantPackageStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "套餐状态不能为空");
        }
        tenantPackageService.changeStatus(packageCode, request.getStatus());
        return ApiResult.ok(null);
    }
}
