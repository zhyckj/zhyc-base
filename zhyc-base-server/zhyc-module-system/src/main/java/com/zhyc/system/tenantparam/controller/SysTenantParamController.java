/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.tenantparam.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.tenantparam.service.SysTenantParamResponse;
import com.zhyc.system.tenantparam.service.SysTenantParamSaveCommand;
import com.zhyc.system.tenantparam.service.SysTenantParamService;
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
 * 租户参数管理接口。
 */
@RestController
@RequestMapping("/system/tenant-params")
public class SysTenantParamController {

    /** 租户参数保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_TENANT_PARAM_SAVE_REQUEST_REQUIRED";

    /** 租户参数业务服务。 */
    private final SysTenantParamService tenantParamService;

    /**
     * 创建租户参数管理接口。
     *
     * @param tenantParamService 租户参数业务服务
     */
    public SysTenantParamController(SysTenantParamService tenantParamService) {
        this.tenantParamService = Objects.requireNonNull(tenantParamService, "租户参数业务服务不能为空");
    }

    /**
     * 查询租户参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 租户参数列表
     */
    @RequiresPermissions("system:tenant-param:query")
    @GetMapping
    public ApiResult<List<SysTenantParamResponse>> listParams(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(tenantParamService.listParams(tenantId));
    }

    /**
     * 保存或更新租户参数。
     *
     * @param request 租户参数保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:tenant-param:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody SysTenantParamSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "租户参数保存请求不能为空");
        }
        tenantParamService.save(new SysTenantParamSaveCommand(request.getTenantId(), request.getParamKey(),
                request.getParamValue(), request.getValueType(), request.isVisible()));
        return ApiResult.ok(null);
    }
}
