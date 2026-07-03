/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenant.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenant.service.SysTenantCreateCommand;
import com.zhyc.system.tenant.service.SysTenantResponse;
import com.zhyc.system.tenant.service.SysTenantService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 系统租户管理接口。
 */
@RestController
@RequestMapping("/system/tenants")
public class SysTenantController {

    /** 租户创建请求缺失错误码。 */
    private static final String ERROR_CREATE_REQUEST_REQUIRED = "ZHYC_SYSTEM_TENANT_CREATE_REQUEST_REQUIRED";
    /** 租户更新请求缺失错误码。 */
    private static final String ERROR_UPDATE_REQUEST_REQUIRED = "ZHYC_SYSTEM_TENANT_UPDATE_REQUEST_REQUIRED";
    /** 租户状态变更请求缺失错误码。 */
    private static final String ERROR_STATUS_REQUEST_REQUIRED = "ZHYC_SYSTEM_TENANT_STATUS_REQUEST_REQUIRED";

    /** 系统租户业务服务。 */
    private final SysTenantService tenantService;

    /**
     * 创建系统租户管理接口。
     *
     * @param tenantService 系统租户业务服务
     */
    public SysTenantController(SysTenantService tenantService) {
        this.tenantService = Objects.requireNonNull(tenantService, "系统租户业务服务不能为空");
    }

    /**
     * 按状态查询系统租户列表。
     *
     * @param status 租户状态
     * @return 系统租户列表
     */
    @RequiresPermissions("system:tenant:query")
    @GetMapping
    public ApiResult<List<SysTenantResponse>> listTenants(@RequestParam("status") String status) {
        return ApiResult.ok(tenantService.listTenants(status));
    }

    /**
     * 查询登录账号可访问的启用租户列表。
     *
     * @param username 登录账号
     * @return 授权租户列表
     */
    @RequiresPermissions("system:tenant:authorized-query")
    @GetMapping("/authorized")
    public ApiResult<List<SysTenantResponse>> listAuthorizedTenants(@RequestParam("username") String username) {
        return ApiResult.ok(tenantService.listAuthorizedTenants(username));
    }

    /**
     * 创建或更新系统租户基础信息。
     *
     * @param request 租户创建请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant:create")
    @PostMapping
    public ApiResult<Void> createTenant(@RequestBody SysTenantCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_CREATE_REQUEST_REQUIRED, "租户创建请求不能为空");
        }
        tenantService.createTenant(new SysTenantCreateCommand(request.getTenantId(),
                request.getName(), request.getPackageId(), request.getIsolationMode(),
                request.getStatus(), request.getContactName(), request.getContactPhone(),
                request.getExpireAt()));
        return ApiResult.ok(null);
    }

    /**
     * 更新系统租户基础信息。
     *
     * @param tenantId 租户业务编码
     * @param request 租户更新请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant:update")
    @PutMapping("/{tenantId}")
    public ApiResult<Void> updateTenant(@PathVariable("tenantId") String tenantId,
                                        @RequestBody SysTenantCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_UPDATE_REQUEST_REQUIRED, "租户更新请求不能为空");
        }
        tenantService.updateTenant(tenantId, new SysTenantCreateCommand(request.getTenantId(),
                request.getName(), request.getPackageId(), request.getIsolationMode(),
                request.getStatus(), request.getContactName(), request.getContactPhone(),
                request.getExpireAt()));
        return ApiResult.ok(null);
    }

    /**
     * 修改系统租户状态。
     *
     * @param tenantId 租户业务编码
     * @param request 租户状态变更请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant:update-status")
    @PutMapping("/{tenantId}/status")
    public ApiResult<Void> changeStatus(@PathVariable("tenantId") String tenantId,
                                        @RequestBody SysTenantStatusRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_STATUS_REQUEST_REQUIRED, "租户状态不能为空");
        }
        tenantService.changeStatus(tenantId, request.getStatus());
        return ApiResult.ok(null);
    }

    /**
     * 删除系统租户主记录。
     *
     * @param tenantId 租户业务编码
     * @return 空响应
     */
    @RequiresPermissions("system:tenant:delete")
    @DeleteMapping("/{tenantId}")
    public ApiResult<Void> deleteTenant(@PathVariable("tenantId") String tenantId) {
        tenantService.deleteTenant(tenantId);
        return ApiResult.ok(null);
    }
}
