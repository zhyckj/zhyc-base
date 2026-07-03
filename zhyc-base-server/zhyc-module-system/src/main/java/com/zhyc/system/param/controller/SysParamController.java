/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

package com.zhyc.system.param.controller;

import com.zhyc.common.api.ApiResult;
import com.zhyc.common.exception.BusinessException;
import com.zhyc.system.param.service.SysParamResponse;
import com.zhyc.system.param.service.SysParamSaveCommand;
import com.zhyc.system.param.service.SysParamService;
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
 * 系统参数管理接口。
 */
@RestController
@RequestMapping("/system/params")
public class SysParamController {

    /** 系统参数保存请求缺失错误码。 */
    private static final String ERROR_SAVE_REQUEST_REQUIRED = "ZHYC_SYSTEM_PARAM_SAVE_REQUEST_REQUIRED";

    /** 系统参数业务服务。 */
    private final SysParamService paramService;

    /**
     * 创建系统参数管理接口。
     *
     * @param paramService 系统参数业务服务
     */
    public SysParamController(SysParamService paramService) {
        this.paramService = Objects.requireNonNull(paramService, "系统参数业务服务不能为空");
    }

    /**
     * 查询租户系统参数列表。
     *
     * @param tenantId 租户业务编码
     * @return 系统参数列表
     */
    @RequiresPermissions("system:param:query")
    @GetMapping
    public ApiResult<List<SysParamResponse>> listParams(@RequestParam("tenantId") String tenantId) {
        return ApiResult.ok(paramService.listParams(tenantId));
    }

    /**
     * 保存或更新租户系统参数。
     *
     * @param request 系统参数保存请求
     * @return 空响应
     */
    @RequiresPermissions("system:param:save")
    @PutMapping
    public ApiResult<Void> save(@RequestBody SysParamSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ERROR_SAVE_REQUEST_REQUIRED, "系统参数保存请求不能为空");
        }
        paramService.save(new SysParamSaveCommand(request.getTenantId(), request.getParamKey(),
                request.getParamValue(), request.getValueType(), request.isSystemFlag(), request.isEditable()));
        return ApiResult.ok(null);
    }
}
