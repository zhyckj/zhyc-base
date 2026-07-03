/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantpackagemodule.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantpackagemodule.service.SysTenantPackageModuleService;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleBindCommand;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleGrantCommand;
import com.zhyc.system.tenantpackagemodule.service.TenantPackageModuleResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 租户套餐模块授权管理接口。
 */
@RestController
@RequestMapping("/system/tenant-package-modules")
public class SysTenantPackageModuleController {

    /** 租户套餐模块授权绑定请求缺失错误码。 */
    private static final String ERROR_BIND_REQUEST_REQUIRED =
            "ZHYC_SYSTEM_TENANT_PACKAGE_MODULE_BIND_REQUEST_REQUIRED";

    /** 租户套餐模块授权业务服务。 */
    private final SysTenantPackageModuleService moduleService;

    /**
     * 创建租户套餐模块授权管理接口。
     *
     * @param moduleService 租户套餐模块授权业务服务
     */
    public SysTenantPackageModuleController(SysTenantPackageModuleService moduleService) {
        this.moduleService = Objects.requireNonNull(moduleService, "租户套餐模块授权业务服务不能为空");
    }

    /**
     * 查询套餐授权资源列表。
     *
     * @param packageId 租户套餐主键
     * @return 套餐授权资源列表
     */
    @RequiresPermissions("system:tenant-package:query")
    @GetMapping("/{packageId}")
    public ApiResult<List<TenantPackageModuleResponse>> listGrants(@PathVariable("packageId") Long packageId) {
        return ApiResult.ok(moduleService.listGrants(packageId));
    }

    /**
     * 绑定套餐授权资源。
     *
     * @param packageId 租户套餐主键
     * @param request 套餐授权绑定请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant-package:update")
    @PutMapping("/{packageId}")
    public ApiResult<Void> bindGrants(@PathVariable("packageId") Long packageId,
                                      @RequestBody TenantPackageModuleBindRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_BIND_REQUEST_REQUIRED, "套餐授权绑定请求不能为空");
        }
        List<TenantPackageModuleGrantCommand> grants = request.getGrants().stream()
                .map(grant -> new TenantPackageModuleGrantCommand(grant.getModuleCode(),
                        grant.getMenuCode(), grant.getPermission()))
                .toList();
        moduleService.bindGrants(new TenantPackageModuleBindCommand(packageId, grants));
        return ApiResult.ok(null);
    }
}
